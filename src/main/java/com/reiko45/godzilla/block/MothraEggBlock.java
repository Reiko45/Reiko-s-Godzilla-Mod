package com.reiko45.godzilla.block;

import com.mojang.serialization.MapCodec;
import com.reiko45.godzilla.entity.kaiju.MothraLarvaEntity;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Mothra's giant egg, found on Infant Island. While a player is nearby it slowly cracks (three stages) and then a
 * Mothra larva hatches; sometimes twins, as in Mothra vs. Godzilla (1964).
 */
public class MothraEggBlock extends Block {
    public static final MapCodec<MothraEggBlock> CODEC = simpleCodec(MothraEggBlock::new);
    public static final IntegerProperty HATCH = IntegerProperty.create("hatch", 0, 2);
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 16, 15);

    public MothraEggBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HATCH, 0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HATCH);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getNearestPlayer(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 32, false) == null) return;
        if (random.nextInt(3) != 0) return;
        int stage = state.getValue(HATCH);
        if (stage < 2) {
            level.setBlock(pos, state.setValue(HATCH, stage + 1), 2);
            level.playSound(null, pos, SoundEvents.TURTLE_EGG_CRACK, SoundSource.BLOCKS, 1.5f, 0.6f);
            level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 10, 0.4, 0.4, 0.4, 0.02);
        } else {
            hatch(level, pos, random);
        }
    }

    private void hatch(ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
        level.playSound(null, pos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 2f, 0.5f);
        level.playSound(null, pos, ModSounds.MOTHRA_CRY.get(), SoundSource.NEUTRAL, 6f, 1.3f);
        int count = random.nextInt(4) == 0 ? 2 : 1;
        for (int i = 0; i < count; i++) {
            MothraLarvaEntity larva = ModEntities.MOTHRA_LARVA.get().create(level);
            if (larva == null) continue;
            larva.moveTo(pos.getX() + 0.5 + i * 3, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360f, 0);
            larva.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.SPAWNER, null);
            level.addFreshEntity(larva);
        }
        level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 60, 1, 1, 1, 0.1);
        for (var player : level.players()) {
            if (player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 64 * 64) {
                player.displayClientMessage(Component.translatable(count == 2 ? "message.reikogodzilla.mothra_twins" : "message.reikogodzilla.mothra_hatched"), true);
            }
        }
    }
}

package com.reiko45.godzilla.block;

import com.mojang.serialization.MapCodec;
import com.reiko45.godzilla.entity.millennium.MeganulaEntity;
import com.reiko45.godzilla.entity.millennium.MeganulonEntity;
import com.reiko45.godzilla.entity.millennium.MegaguirusEntity;
import com.reiko45.godzilla.registry.ModBlocks;
import com.reiko45.godzilla.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A Meganulon egg mass (Godzilla vs. Megaguirus). Wet, it keeps budding off Meganulon larvae. Meganula bring stolen
 * kaiju energy back to it; feed it five times and Megaguirus, the queen, bursts out.
 */
public class MeganulonEggBlock extends Block {
    public static final MapCodec<MeganulonEggBlock> CODEC = simpleCodec(MeganulonEggBlock::new);
    public static final IntegerProperty FED = IntegerProperty.create("fed", 0, 4);
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 10, 16);

    public MeganulonEggBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FED, 0));
    }

    @Override
    protected MapCodec<MeganulonEggBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    private static boolean isWet(ServerLevel level, BlockPos pos) {
        if (level.isRainingAt(pos.above())) return true;
        for (Direction d : Direction.values()) if (level.getFluidState(pos.relative(d)).is(FluidTags.WATER)) return true;
        return false;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!isWet(level, pos) || random.nextInt(4) != 0) return;
        if (level.getNearestPlayer(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 48, false) == null) return;
        AABB area = new AABB(pos).inflate(32);
        int brood = level.getEntitiesOfClass(MeganulonEntity.class, area).size() + level.getEntitiesOfClass(MeganulaEntity.class, area).size();
        if (brood >= 6) return;
        MeganulonEntity larva = ModEntities.MEGANULON.get().create(level);
        if (larva == null) return;
        larva.moveTo(pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, random.nextFloat() * 360f, 0);
        larva.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.SPAWNER, null);
        larva.setNest(pos);
        level.addFreshEntity(larva);
        level.playSound(null, pos, SoundEvents.FROGSPAWN_HATCH, SoundSource.BLOCKS, 1f, 0.6f);
    }

    /** A Meganula delivers its stolen energy. Returns false if the egg mass is gone. */
    public static boolean feed(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(ModBlocks.MEGANULON_EGG.get())) return false;
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5, 12, 0.4, 0.3, 0.4, 0.02);
        int fed = state.getValue(FED);
        if (fed < 4) {
            level.setBlock(pos, state.setValue(FED, fed + 1), 3);
            return true;
        }
        level.removeBlock(pos, false);
        MegaguirusEntity queen = ModEntities.MEGAGUIRUS.get().create(level);
        if (queen != null) {
            queen.moveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, level.random.nextFloat() * 360f, 0);
            queen.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
            level.addFreshEntity(queen);
            level.playSound(null, pos, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 6f, 1.4f);
            for (var p : level.players()) {
                if (p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 200 * 200) {
                    p.sendSystemMessage(Component.translatable("message.reikogodzilla.megaguirus_born").withStyle(ChatFormatting.DARK_GREEN));
                }
            }
        }
        return true;
    }
}

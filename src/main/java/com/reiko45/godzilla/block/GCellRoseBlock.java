package com.reiko45.godzilla.block;

import com.mojang.serialization.MapCodec;
import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.kaiju.BiollanteEntity;
import com.reiko45.godzilla.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * A rose spliced with G-Cells (Godzilla vs. Biollante, 1989). Plant it on soil within 4 blocks of water; while a player
 * is near it grows strangely... and eventually becomes Biollante.
 */
public class GCellRoseBlock extends BushBlock {
    public static final MapCodec<GCellRoseBlock> CODEC = simpleCodec(GCellRoseBlock::new);
    public static final IntegerProperty GROWTH = IntegerProperty.create("growth", 0, 15);

    public GCellRoseBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(GROWTH, 0));
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(GROWTH);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT) || state.is(net.minecraft.world.level.block.Blocks.FARMLAND);
    }

    private static boolean nearWater(LevelReader level, BlockPos pos) {
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-4, -2, -4), pos.offset(4, 1, 4))) {
            if (level.getFluidState(p).is(FluidTags.WATER)) return true;
        }
        return false;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!nearWater(level, pos) || level.getNearestPlayer(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 48, false) == null) return;
        int growth = state.getValue(GROWTH) + 1;
        int needed = Math.min(15, GodzillaConfig.get(GodzillaConfig.BIOLLANTE_GROWTH_STAGES, 6));
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 15, 0.6, 0.6, 0.6, 0.02);
        if (growth < needed) {
            level.setBlock(pos, state.setValue(GROWTH, growth), 2);
            level.playSound(null, pos, SoundEvents.BIG_DRIPLEAF_TILT_DOWN, SoundSource.BLOCKS, 1f, 0.5f);
            return;
        }
        level.removeBlock(pos, false);
        BiollanteEntity biollante = ModEntities.BIOLLANTE.get().create(level);
        if (biollante == null) return;
        biollante.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360f, 0);
        biollante.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.SPAWNER, null);
        level.addFreshEntity(biollante);
        level.playSound(null, pos, SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 8f, 0.5f);
        for (var p : level.players()) {
            if (p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 128 * 128) {
                p.sendSystemMessage(Component.translatable("message.reikogodzilla.biollante_born").withStyle(ChatFormatting.DARK_GREEN));
            }
        }
    }
}

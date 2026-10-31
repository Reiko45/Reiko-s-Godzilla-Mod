package com.reiko45.godzilla.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Function;

/**
 * A kaiju egg that cracks in stages while a player is nearby, then hatches (Mothra Egg, Godzilla Egg).
 */
public abstract class HatchingEggBlock extends Block {
    public static final IntegerProperty HATCH = IntegerProperty.create("hatch", 0, 2);
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 16, 15);

    protected HatchingEggBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HATCH, 0));
    }

    protected static <T extends HatchingEggBlock> MapCodec<T> codecFor(Function<Properties, T> factory) {
        return simpleCodec(factory);
    }

    /** Spawn whatever hatches. The block has already been removed. */
    protected abstract void hatch(ServerLevel level, BlockPos pos, RandomSource random);

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
            level.removeBlock(pos, false);
            level.playSound(null, pos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 2f, 0.5f);
            hatch(level, pos, random);
        }
    }
}

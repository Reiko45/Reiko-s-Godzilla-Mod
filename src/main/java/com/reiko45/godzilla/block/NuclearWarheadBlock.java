package com.reiko45.godzilla.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * An inert nuclear warhead (the bait the MUTOs stole in 2014, the bomb that revived Godzilla in 2019). It won't go
 * off, but Titans can smell its radiation from far away.
 */
public class NuclearWarheadBlock extends RadiationSourceBlock {
    public static final MapCodec<NuclearWarheadBlock> CODEC = simpleCodec(NuclearWarheadBlock::new);
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 15, 13);

    public NuclearWarheadBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<NuclearWarheadBlock> codec() {
        return CODEC;
    }

    @Override
    public int energy() {
        return 60;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}

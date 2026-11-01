package com.reiko45.godzilla.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Something MonsterVerse Titans feed on: Godzilla soaks up its radiation to charge his breath, MUTOs and Scylla hunt
 * it down. The block entity is only a marker, so nearby sources can be found without scanning every block.
 */
public abstract class RadiationSourceBlock extends BaseEntityBlock {
    protected RadiationSourceBlock(Properties properties) {
        super(properties);
    }

    /** How much charge a Titan gets from absorbing this (Godzilla becomes Energized at 100). */
    public abstract int energy();

    /** Tiamat's stored solar radiation: Godzilla evolves when he absorbs it. */
    public boolean isSolar() {
        return false;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RadiationSourceBlockEntity(pos, state);
    }
}

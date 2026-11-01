package com.reiko45.godzilla.block;

import com.reiko45.godzilla.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Marker for {@link RadiationSourceBlock}s (see {@link com.reiko45.godzilla.world.Radiation}). */
public class RadiationSourceBlockEntity extends BlockEntity {
    public RadiationSourceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RADIATION_SOURCE.get(), pos, state);
    }
}

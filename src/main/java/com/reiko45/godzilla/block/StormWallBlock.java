package com.reiko45.godzilla.block;

import com.mojang.serialization.MapCodec;
import com.reiko45.godzilla.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/** Invisible heart of Skull Island's permanent storm wall. See {@link StormWallBlockEntity}. */
public class StormWallBlock extends BaseEntityBlock {
    public static final MapCodec<StormWallBlock> CODEC = simpleCodec(StormWallBlock::new);

    public StormWallBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<StormWallBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StormWallBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.STORM_WALL.get(), StormWallBlockEntity::serverTick);
    }
}

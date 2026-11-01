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

/** Kiryu's maintenance gantry (the Hachioji hangar): recharges any Kiryu parked within 10 blocks. */
public class KiryuChargerBlock extends BaseEntityBlock {
    public static final MapCodec<KiryuChargerBlock> CODEC = simpleCodec(KiryuChargerBlock::new);

    public KiryuChargerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<KiryuChargerBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new KiryuChargerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.KIRYU_CHARGER.get(), KiryuChargerBlockEntity::serverTick);
    }
}

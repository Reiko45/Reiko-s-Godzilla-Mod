package com.reiko45.godzilla.block;

import com.mojang.serialization.MapCodec;
import com.reiko45.godzilla.registry.ModBlockEntities;
import com.reiko45.godzilla.world.AlienSignal;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * An alien control transmitter: the Nebula M control room in the Godzilla Tower, Seatopia's guidance beacon, the
 * Black Hole aliens' relay, the Xilien core. While it stands, every monster in range that the signal can reach fights
 * for the aliens. Break it and they're freed. Creative players can sneak + use it to change the signal.
 */
public class AlienTransmitterBlock extends BaseEntityBlock {
    public static final MapCodec<AlienTransmitterBlock> CODEC = simpleCodec(AlienTransmitterBlock::new);
    public static final EnumProperty<AlienSignal> SIGNAL = EnumProperty.create("signal", AlienSignal.class);

    public AlienTransmitterBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SIGNAL, AlienSignal.XILIEN));
    }

    @Override
    protected MapCodec<AlienTransmitterBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SIGNAL);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getAbilities().instabuild || !player.isSecondaryUseActive()) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        AlienSignal next = state.getValue(SIGNAL).next();
        level.setBlock(pos, state.setValue(SIGNAL, next), 3);
        level.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1f, 1.4f);
        player.displayClientMessage(Component.translatable("message.reikogodzilla.transmitter_signal",
                Component.translatable("signal.reikogodzilla." + next.getSerializedName())).withStyle(next.chat), true);
        return InteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            level.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 4f, 0.5f);
            for (Player p : level.players()) {
                if (p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 160 * 160) {
                    p.displayClientMessage(Component.translatable("message.reikogodzilla.transmitter_destroyed",
                            Component.translatable("signal.reikogodzilla." + state.getValue(SIGNAL).getSerializedName())).withStyle(ChatFormatting.GREEN), false);
                }
            }
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlienTransmitterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.ALIEN_TRANSMITTER.get(), AlienTransmitterBlockEntity::serverTick);
    }
}

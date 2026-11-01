package com.reiko45.godzilla.block;

import com.mojang.serialization.MapCodec;
import com.reiko45.godzilla.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * The ORCA (King of the Monsters): Emma Russell's bioacoustic device, broadcasting Titan frequencies.
 * Use it to switch it on or off (or power it with redstone); sneak + use changes the signal:
 * <ul>
 *     <li>Calm: Titans in range drop their fight and settle down (it soothed the Mothra larva).</li>
 *     <li>Lure: Titans in range head for the ORCA (Fenway Park).</li>
 *     <li>Alpha: a copied Alpha signal. Wakes dormant Titans far and wide and riles up those nearby. Dangerous.</li>
 * </ul>
 */
public class OrcaBlock extends BaseEntityBlock {
    public static final MapCodec<OrcaBlock> CODEC = simpleCodec(OrcaBlock::new);
    public static final EnumProperty<Mode> MODE = EnumProperty.create("mode", Mode.class);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public enum Mode implements StringRepresentable {
        CALM("calm"), LURE("lure"), ALPHA("alpha");
        private final String name;

        Mode(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public OrcaBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(MODE, Mode.CALM).setValue(ACTIVE, false).setValue(POWERED, false));
    }

    @Override
    protected MapCodec<OrcaBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MODE, ACTIVE, POWERED);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public static boolean isBroadcasting(BlockState state) {
        return state.getValue(ACTIVE) || state.getValue(POWERED);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        BlockState next;
        if (player.isSecondaryUseActive()) {
            Mode mode = Mode.values()[(state.getValue(MODE).ordinal() + 1) % Mode.values().length];
            next = state.setValue(MODE, mode);
            player.displayClientMessage(Component.translatable("message.reikogodzilla.orca_mode",
                    Component.translatable("orca.reikogodzilla." + mode.getSerializedName())).withStyle(ChatFormatting.AQUA), true);
        } else {
            next = state.setValue(ACTIVE, !state.getValue(ACTIVE));
            player.displayClientMessage(Component.translatable(next.getValue(ACTIVE) ? "message.reikogodzilla.orca_on" : "message.reikogodzilla.orca_off",
                    Component.translatable("orca.reikogodzilla." + next.getValue(MODE).getSerializedName())).withStyle(ChatFormatting.AQUA), true);
        }
        level.setBlock(pos, next, 3);
        level.playSound(null, pos, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 1f, player.isSecondaryUseActive() ? 1.4f : 0.8f);
        return InteractionResult.CONSUME;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos from, boolean moving) {
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) level.setBlock(pos, state.setValue(POWERED, powered), 3);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OrcaBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.ORCA.get(), OrcaBlockEntity::serverTick);
    }
}

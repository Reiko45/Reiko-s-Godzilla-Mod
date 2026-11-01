package com.reiko45.godzilla.block;

import com.mojang.serialization.MapCodec;
import com.reiko45.godzilla.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * The sacred stone at a GMK Guardian's shrine (Baragon at Mount Myoko, Mothra at Lake Ikeda, King Ghidorah under the
 * forest by Mount Fuji). When GMK Godzilla comes near, the Guardian rises to fight him. Break the stone and the
 * Guardian rises anyway, and hunts you.
 */
public class GuardianShrineBlock extends BaseEntityBlock {
    public static final MapCodec<GuardianShrineBlock> CODEC = simpleCodec(GuardianShrineBlock::new);
    public static final EnumProperty<Guardian> GUARDIAN = EnumProperty.create("guardian", Guardian.class);
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 15, 14);

    public enum Guardian implements StringRepresentable {
        BARAGON("baragon"), MOTHRA("mothra"), GHIDORAH("ghidorah");
        private final String name;

        Guardian(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public GuardianShrineBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(GUARDIAN, Guardian.BARAGON));
    }

    @Override
    protected MapCodec<GuardianShrineBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(GUARDIAN);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /** Desecration. */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof GuardianShrineBlockEntity shrine) shrine.awaken(player);
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GuardianShrineBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.GUARDIAN_SHRINE.get(), GuardianShrineBlockEntity::serverTick);
    }
}

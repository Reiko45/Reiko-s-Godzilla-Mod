package com.reiko45.godzilla.block;

import com.mojang.serialization.MapCodec;
import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.KaijuEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A storm beacon (Godzilla Minus Zero): Ghidorah travels with storm systems, and the plan was to steer the weather to
 * bring it to Godzilla. Power the beacon with redstone and it seeds a thunderstorm; every Ghidorah within range heads
 * for it.
 */
public class StormBeaconBlock extends Block {
    public static final MapCodec<StormBeaconBlock> CODEC = simpleCodec(StormBeaconBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public StormBeaconBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected MapCodec<StormBeaconBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos from, boolean moving) {
        boolean powered = level.hasNeighborSignal(pos);
        if (powered == state.getValue(POWERED)) return;
        level.setBlock(pos, state.setValue(POWERED, powered), 3);
        if (powered && level instanceof ServerLevel server) {
            server.setWeatherParameters(0, 6000, true, true);
            server.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.BLOCKS, 4f, 0.7f);
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 60, 0.4, 3, 0.4, 0.3);
            int lured = 0;
            for (var g : server.getEntitiesOfClass(com.reiko45.godzilla.entity.reiwa.MinusZeroGhidorahEntity.class, new AABB(pos).inflate(256))) {
                g.setDestination(pos);
                g.setTarget(null);
                lured++;
            }
            for (ServerPlayer p : server.players()) {
                if (p.distanceToSqr(Vec3.atCenterOf(pos)) < 64 * 64) {
                    p.displayClientMessage(Component.translatable(lured > 0 ? "message.reikogodzilla.storm_lure" : "message.reikogodzilla.storm_seeded"), true);
                }
            }
        }
    }
}

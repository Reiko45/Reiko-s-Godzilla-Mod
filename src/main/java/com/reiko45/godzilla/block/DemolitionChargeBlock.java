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
 * Demolition charges (Shin Godzilla): the skyscrapers along Godzilla's path were rigged to fall on him. Power a charge
 * with redstone and the 7x7 column of blocks above it comes down as falling rubble, crushing whatever is underneath.
 * A kaiju caught in it is stunned, and Shin Godzilla is knocked closer to the ground.
 */
public class DemolitionChargeBlock extends Block {
    public static final MapCodec<DemolitionChargeBlock> CODEC = simpleCodec(DemolitionChargeBlock::new);

    public DemolitionChargeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<DemolitionChargeBlock> codec() {
        return CODEC;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos from, boolean moving) {
        if (level instanceof ServerLevel server && level.hasNeighborSignal(pos)) detonate(server, pos);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        if (!old.is(state.getBlock()) && level instanceof ServerLevel server && level.hasNeighborSignal(pos)) detonate(server, pos);
    }

    public static void detonate(ServerLevel level, BlockPos pos) {
        level.removeBlock(pos, false);
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2.0f, Level.ExplosionInteraction.NONE);
        int budget = 600;
        RandomSource random = level.getRandom();
        for (int dy = 1; dy <= 40 && budget > 0; dy++) {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos p = pos.offset(dx, dy, dz);
                    BlockState s = level.getBlockState(p);
                    if (s.isAir() || !s.getFluidState().isEmpty() || s.hasBlockEntity() || s.getBlock().getExplosionResistance() > 50f) continue;
                    var falling = net.minecraft.world.entity.item.FallingBlockEntity.fall(level, p, s);
                    falling.setHurtsEntities(3.0f, 80);
                    falling.dropItem = false;
                    falling.setDeltaMovement((random.nextDouble() - 0.5) * 0.4, -0.2, (random.nextDouble() - 0.5) * 0.4);
                    if (--budget <= 0) break;
                }
                if (budget <= 0) break;
            }
        }
        AABB column = new AABB(pos).inflate(5, 0, 5).expandTowards(0, 40, 0).inflate(2, 0, 2);
        for (KaijuEntity kaiju : level.getEntitiesOfClass(KaijuEntity.class, column)) {
            kaiju.stun(80);
            kaiju.hurt(level.damageSources().fallingBlock(null), 60f);
            if (kaiju instanceof com.reiko45.godzilla.entity.reiwa.ShinGodzillaEntity shin) shin.addKnockdown(0.45f);
        }
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 6f, 0.5f);
    }
}

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
 * Liquid nanometal (City on the Edge of Battle): the trap that poured over Godzilla Earth and hardened around him. Any
 * kaiju wading into it is rooted in place; people are slowed to a crawl. Godzilla Earth's heat venting melts it.
 */
public class LiquidNanometalBlock extends Block {
    public static final MapCodec<LiquidNanometalBlock> CODEC = simpleCodec(LiquidNanometalBlock::new);
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 12, 16);

    public LiquidNanometalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<LiquidNanometalBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return net.minecraft.world.phys.shapes.Shapes.empty();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide) return;
        if (entity instanceof KaijuEntity kaiju) {
            if (kaiju.getFaction() == com.reiko45.godzilla.entity.KaijuFaction.HUMAN) return;
            kaiju.stun(30);
            if (level.getGameTime() % 10 == 0 && level instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.ELECTRIC_SPARK, entity.getX(), entity.getY() + 1, entity.getZ(), 10, entity.getBbWidth() * 0.4, 0.5, entity.getBbWidth() * 0.4, 0.05);
            }
        } else {
            entity.makeStuckInBlock(state, new Vec3(0.3, 0.2, 0.3));
        }
    }
}

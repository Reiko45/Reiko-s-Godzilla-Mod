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
 * The Exif's Garbetrium totem (GODZILLA: The Planet Eater). Activating it opens the way for Ghidorah, the
 * extradimensional Planet Eater, whose necks pour down through rifts in the sky. While the totem stands, the Exif are
 * "observing" Ghidorah for our world and nothing can touch it. Break the totem and it becomes mortal.
 */
public class GarbetriumTotemBlock extends Block {
    public static final MapCodec<GarbetriumTotemBlock> CODEC = simpleCodec(GarbetriumTotemBlock::new);

    public GarbetriumTotemBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<GarbetriumTotemBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level instanceof ServerLevel server && player instanceof ServerPlayer sp) {
            if (!server.getEntitiesOfClass(com.reiko45.godzilla.entity.reiwa.PlanetEaterGhidorahEntity.class, new AABB(pos).inflate(256)).isEmpty()) {
                sp.displayClientMessage(Component.translatable("message.reikogodzilla.planet_eater_here"), true);
            } else {
                com.reiko45.godzilla.world.ModernEvents.summonPlanetEater(server, pos, sp);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel server) {
            var necks = server.getEntitiesOfClass(com.reiko45.godzilla.entity.reiwa.PlanetEaterGhidorahEntity.class, new AABB(pos).inflate(256));
            for (var neck : necks) neck.observerLost();
            if (!necks.isEmpty()) {
                for (ServerPlayer p : server.players()) {
                    if (p.distanceToSqr(Vec3.atCenterOf(pos)) < 256 * 256) p.sendSystemMessage(Component.translatable("message.reikogodzilla.observer_broken")
                            .withStyle(net.minecraft.ChatFormatting.GOLD));
                }
            }
            server.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 40, 0.4, 0.6, 0.4, 0.2);
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) level.addParticle(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.4, pos.getZ() + 0.5,
                (random.nextDouble() - 0.5) * 2, -random.nextDouble(), (random.nextDouble() - 0.5) * 2);
    }
}

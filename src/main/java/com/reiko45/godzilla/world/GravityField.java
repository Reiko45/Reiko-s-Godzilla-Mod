package com.reiko45.godzilla.world;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Minus Zero's Ghidorah wraps its lightning in anti-gravity: buildings and trains rise into the air, hang there, and
 * come crashing down. Blocks become falling blocks thrown upward (they crush whatever they land on); living things
 * float up and drop. Obsidian, bedrock and other blast-proof blocks stay put, and each lift is capped for performance.
 */
public final class GravityField {
    private GravityField() {}

    public static void lift(ServerLevel level, @Nullable Entity source, Vec3 center, int radius, int maxBlocks) {
        RandomSource random = level.getRandom();
        int lifted = 0;
        boolean grief = source == null || KaijuDestruction.canGrief(level, source);
        if (grief) {
            BlockPos c = BlockPos.containing(center);
            for (int attempt = 0; attempt < maxBlocks * 3 && lifted < maxBlocks; attempt++) {
                BlockPos pos = c.offset(random.nextInt(radius * 2 + 1) - radius, random.nextInt(radius * 2 + 1) - radius, random.nextInt(radius * 2 + 1) - radius);
                BlockState state = level.getBlockState(pos);
                if (state.isAir() || !state.getFluidState().isEmpty() || state.getBlock().getExplosionResistance() > 50f) continue;
                if (!KaijuDestruction.isBreakable(level, pos, state) || state.hasBlockEntity()) continue;
                if (!level.getBlockState(pos.above()).isAir() && random.nextInt(3) != 0) continue;
                FallingBlockEntity block = FallingBlockEntity.fall(level, pos, state);
                block.setDeltaMovement((random.nextDouble() - 0.5) * 0.3, 0.9 + random.nextDouble() * 0.6, (random.nextDouble() - 0.5) * 0.3);
                block.setHurtsEntities(2.5f, 60);
                block.dropItem = false;
                block.hurtMarked = true;
                lifted++;
            }
        }
        AABB area = new AABB(center, center).inflate(radius + 2);
        for (Entity e : level.getEntities(source, area)) {
            if (e instanceof Player p && (p.isCreative() || p.isSpectator())) continue;
            if (e instanceof KaijuEntity k) {
                if (source instanceof KaijuEntity s && (s.isAlliedTo(k) || s == k)) continue;
                k.stun(40);
                k.setDeltaMovement(k.getDeltaMovement().add(0, 0.9, 0));
                k.hurtMarked = true;
            } else if (e instanceof LivingEntity l) {
                l.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 50, 3));
            } else if (e instanceof net.minecraft.world.entity.vehicle.VehicleEntity || e instanceof net.minecraft.world.entity.item.ItemEntity) {
                e.setDeltaMovement(e.getDeltaMovement().add(0, 1.2, 0));
                e.hurtMarked = true;
            }
        }
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 1, center.z, 60, radius * 0.5, radius * 0.4, radius * 0.5, 0.3);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y + 1, center.z, 40, radius * 0.5, radius * 0.4, radius * 0.5, 0.4);
        level.playSound(null, center.x, center.y, center.z, net.minecraft.sounds.SoundEvents.BEACON_DEACTIVATE, net.minecraft.sounds.SoundSource.HOSTILE, 5f, 0.4f);
    }
}

package com.reiko45.godzilla.world;

import com.reiko45.godzilla.block.FalloutAshBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * The aftermath of Minus One Godzilla's heat ray: a black rain falls over the blast zone (a lingering cloud that
 * sickens anything standing in it) and fallout ash settles on the ground.
 */
public final class Fallout {
    private Fallout() {}

    public static void blackRain(ServerLevel level, Vec3 center, float radius, int duration, @Nullable LivingEntity source) {
        AreaEffectCloud cloud = new AreaEffectCloud(level, center.x, center.y, center.z);
        cloud.setRadius(Math.min(radius, 24f));
        cloud.setRadiusPerTick(0);
        cloud.setDuration(duration);
        cloud.setWaitTime(10);
        cloud.setParticle(ParticleTypes.ASH);
        cloud.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0));
        cloud.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 1));
        if (source != null) cloud.setOwner(source);
        level.addFreshEntity(cloud);
        FalloutAshBlock.scatter(level, BlockPos.containing(center), (int) radius, (int) (radius * 3));
        level.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y + 12, center.z, 120, radius * 0.5, 4, radius * 0.5, 0.01);
    }
}

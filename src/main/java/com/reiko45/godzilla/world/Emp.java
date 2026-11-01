package com.reiko45.godzilla.world;

import com.reiko45.godzilla.entity.EmpSensitive;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * MUTO electromagnetic pulses (Godzilla, 2014): electronics in range go dead until the source moves away. Machines
 * stall, aircraft drop out of the sky, and gliding players lose lift.
 */
public final class Emp {
    private Emp() {}

    public static void pulse(ServerLevel level, Vec3 center, double radius, int ticks, boolean visible) {
        AABB box = new AABB(center, center).inflate(radius);
        double r2 = radius * radius;
        for (Entity e : level.getEntities((Entity) null, box, e -> e instanceof EmpSensitive)) {
            if (e.distanceToSqr(center) <= r2) ((EmpSensitive) e).applyEmp(ticks);
        }
        for (ServerPlayer p : level.players()) {
            if (p.isFallFlying() && p.distanceToSqr(center) <= r2) p.stopFallFlying();
        }
        if (!visible) return;
        level.playSound(null, center.x, center.y, center.z, SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 6f, 0.5f);
        int points = 64;
        for (int i = 0; i < points; i++) {
            double a = i * Math.PI * 2 / points;
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x + Math.cos(a) * radius * 0.5, center.y + 1,
                    center.z + Math.sin(a) * radius * 0.5, 2, 0.3, 0.6, 0.3, 0.1);
        }
        level.sendParticles(ParticleTypes.FLASH, center.x, center.y + 2, center.z, 1, 0, 0, 0, 0);
    }
}

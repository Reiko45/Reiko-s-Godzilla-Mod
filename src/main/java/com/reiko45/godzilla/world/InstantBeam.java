package com.reiko45.godzilla.world;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * A thin, instant beam drawn with particles: Shin Godzilla's dorsal beams, Godzilla Earth's anti-air snipe. Unlike a
 * charged ability beam it is fired many times a second without stopping the kaiju.
 */
public final class InstantBeam {
    private InstantBeam() {}

    public static DustParticleOptions dust(float r, float g, float b, float size) {
        return new DustParticleOptions(new Vector3f(r, g, b), size);
    }

    /** Draws the beam and hurts whatever it crosses (the target, and anything in the way). */
    public static void fire(ServerLevel level, Entity shooter, Vec3 from, Vec3 to, ParticleOptions particle, DamageSource damage, float amount) {
        double length = from.distanceTo(to);
        int steps = (int) Math.min(240, Math.max(4, length / 0.7));
        for (int i = 0; i <= steps; i++) {
            Vec3 p = from.lerp(to, i / (double) steps);
            level.sendParticles(particle, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0);
        }
        KaijuDestruction.damageAlongSegment(shooter, from, to, 0.4, damage, amount, e -> !(e instanceof Player p && (p.isCreative() || p.isSpectator()))
                && !(shooter instanceof KaijuEntity k && (k.hasPassenger(e) || k.isAlliedTo(e))), e -> {});
    }

    /** Is something up in the air that an anti-air defence would shoot at? */
    public static boolean isAirborne(Entity e) {
        if (e instanceof Player p) return p.isFallFlying() || (!p.onGround() && !p.isInWater() && p.getVehicle() == null && p.fallDistance > 3);
        if (e instanceof com.reiko45.godzilla.entity.FlyingKaijuEntity f) return f.isFlying();
        if (e instanceof LivingEntity l && l.getVehicle() instanceof com.reiko45.godzilla.entity.FlyingKaijuEntity f) return f.isFlying();
        return e instanceof net.minecraft.world.entity.projectile.Projectile;
    }
}

package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.KaijuEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/** SpaceGodzilla's gravitational tornado: everything small nearby is lifted, spun and flung away. */
public class GravityTornadoAbility extends KaijuAbility {
    public GravityTornadoAbility() {
        super("gravity_tornado", 20, 60, 400, "charge", "tornado");
        this.minRange = 0;
        this.maxRange = 26;
        this.useChance = 0.025f;
    }

    @Override
    public void activate(KaijuEntity kaiju, @Nullable LivingEntity target) {
        kaiju.level().playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), SoundEvents.ELYTRA_FLYING, SoundSource.HOSTILE, 6f, 0.5f);
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (!(kaiju.level() instanceof ServerLevel level)) return;
        double radius = maxRange * kaiju.getScale();
        for (LivingEntity e : AreaAbility.victims(kaiju, radius)) {
            if (e instanceof KaijuEntity) continue;
            Vec3 offset = e.position().subtract(kaiju.position()).multiply(1, 0, 1);
            Vec3 tangent = new Vec3(-offset.z, 0, offset.x).normalize();
            boolean release = tick >= activeTicks - 5;
            Vec3 push = release ? offset.normalize().scale(1.6).add(0, 0.6, 0) : tangent.scale(0.5).add(0, 0.22, 0).add(offset.normalize().scale(-0.08));
            e.setDeltaMovement(push);
            e.hurtMarked = true;
            e.fallDistance = 0;
            if (tick % 10 == 0) e.hurt(kaiju.damageSources().mobAttack(kaiju), 2f);
        }
        if (tick % 2 == 0) {
            for (int i = 0; i < 16; i++) {
                double a = kaiju.getRandom().nextDouble() * Math.PI * 2 + tick * 0.3;
                double d = kaiju.getRandom().nextDouble() * radius;
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, kaiju.getX() + Math.cos(a) * d, kaiju.getY() + kaiju.getRandom().nextDouble() * 12,
                        kaiju.getZ() + Math.sin(a) * d, 1, 0, 0.1, 0, 0.05);
            }
        }
    }
}

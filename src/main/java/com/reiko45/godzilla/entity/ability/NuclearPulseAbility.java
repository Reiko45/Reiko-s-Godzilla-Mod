package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/** Heisei Godzilla's nuclear pulse: a burst of energy from the whole body when enemies get too close. */
public class NuclearPulseAbility extends KaijuAbility {
    public NuclearPulseAbility() {
        super("nuclear_pulse", 20, 10, 500, "charge", "pulse");
        this.minRange = 0;
        this.maxRange = 5;
        this.useChance = 0.04f;
    }

    @Override
    protected boolean extraStartCheck(KaijuEntity kaiju, LivingEntity target) {
        return kaiju.getHealth() < kaiju.getMaxHealth() * 0.8f;
    }

    @Override
    public void tickCharge(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (kaiju.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, kaiju.getX(), kaiju.getY(0.5), kaiju.getZ(), 20,
                    kaiju.getBbWidth() * 0.6, kaiju.getBbHeight() * 0.4, kaiju.getBbWidth() * 0.6, 0.1);
        }
    }

    @Override
    public void activate(KaijuEntity kaiju, @Nullable LivingEntity target) {
        if (!(kaiju.level() instanceof ServerLevel level)) return;
        double radius = 10 * kaiju.getScale();
        level.playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 6f, 0.6f);
        for (int i = 0; i < 48; i++) {
            double a = i / 48.0 * Math.PI * 2;
            level.sendParticles(ParticleTypes.SONIC_BOOM, kaiju.getX() + Math.cos(a) * radius * 0.6, kaiju.getY(0.4),
                    kaiju.getZ() + Math.sin(a) * radius * 0.6, 1, 0, 0, 0, 0);
        }
        for (LivingEntity e : AreaAbility.victims(kaiju, radius)) {
            KaijuDestruction.hurtIgnoringCooldown(e, ModDamageTypes.source(level, ModDamageTypes.NUCLEAR_PULSE, kaiju), 30f);
            Vec3 away = e.position().subtract(kaiju.position()).multiply(1, 0, 1).normalize();
            e.push(away.x * 3.0, 0.8, away.z * 3.0);
        }
    }
}

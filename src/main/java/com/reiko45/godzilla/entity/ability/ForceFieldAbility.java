package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.KaijuEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

/**
 * Showa Mechagodzilla spins its head to raise a barrier that blocks all damage for a few seconds.
 * The kaiju checks {@link #isShielding(KaijuEntity)} in its hurt() method.
 */
public class ForceFieldAbility extends KaijuAbility {
    public ForceFieldAbility() {
        super("force_field", 10, 80, 500, "charge", "shield");
        this.minRange = 0;
        this.maxRange = 64;
        this.useChance = 0.08f;
    }

    public static boolean isShielding(KaijuEntity kaiju) {
        return kaiju.getActiveAbility() instanceof ForceFieldAbility && kaiju.getAbilityPhase() == KaijuEntity.PHASE_ACTIVE;
    }

    @Override
    protected boolean extraStartCheck(KaijuEntity kaiju, LivingEntity target) {
        return kaiju.hurtTime > 0 && kaiju.getHealth() < kaiju.getMaxHealth() * 0.85f;
    }

    @Override
    public void activate(KaijuEntity kaiju, @Nullable LivingEntity target) {
        kaiju.level().playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 5f, 0.5f);
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (!(kaiju.level() instanceof ServerLevel level) || tick % 2 != 0) return;
        double r = kaiju.getBbHeight() * 0.65;
        for (int i = 0; i < 24; i++) {
            double theta = kaiju.getRandom().nextDouble() * Math.PI * 2;
            double phi = Math.acos(2 * kaiju.getRandom().nextDouble() - 1);
            level.sendParticles(ParticleTypes.END_ROD,
                    kaiju.getX() + r * Math.sin(phi) * Math.cos(theta), kaiju.getY(0.5) + r * Math.cos(phi), kaiju.getZ() + r * Math.sin(phi) * Math.sin(theta),
                    1, 0, 0, 0, 0);
        }
    }

    @Override
    public void stop(KaijuEntity kaiju) {
        kaiju.level().playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 4f, 0.5f);
    }
}

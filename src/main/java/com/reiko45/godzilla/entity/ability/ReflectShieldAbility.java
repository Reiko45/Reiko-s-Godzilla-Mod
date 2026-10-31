package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.KaijuEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

/**
 * A barrier that bounces beams back at whoever fired them (SpaceGodzilla's Photon Reactive Shield). The kaiju checks
 * {@link #isActive(KaijuEntity)} from {@code reflectsBeams()}.
 */
public class ReflectShieldAbility extends KaijuAbility {
    private final ParticleOptions particle;

    public ReflectShieldAbility(String name, ParticleOptions particle) {
        super(name, 8, 80, 360, "charge", "shield");
        this.particle = particle;
        this.minRange = 0;
        this.maxRange = 64;
        this.useChance = 0.1f;
    }

    public static boolean isActive(KaijuEntity kaiju) {
        return kaiju.getActiveAbility() instanceof ReflectShieldAbility && kaiju.getAbilityPhase() == KaijuEntity.PHASE_ACTIVE;
    }

    @Override
    protected boolean extraStartCheck(KaijuEntity kaiju, LivingEntity target) {
        return kaiju.hurtTime > 0;
    }

    @Override
    public void activate(KaijuEntity kaiju, @Nullable LivingEntity target) {
        kaiju.level().playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 6f, 0.5f);
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (!(kaiju.level() instanceof ServerLevel level) || tick % 2 != 0) return;
        double r = kaiju.getBbHeight() * 0.6;
        for (int i = 0; i < 20; i++) {
            double theta = kaiju.getRandom().nextDouble() * Math.PI * 2;
            double phi = Math.acos(2 * kaiju.getRandom().nextDouble() - 1);
            level.sendParticles(particle, kaiju.getX() + r * Math.sin(phi) * Math.cos(theta), kaiju.getY(0.5) + r * Math.cos(phi),
                    kaiju.getZ() + r * Math.sin(phi) * Math.sin(theta), 1, 0, 0, 0, 0);
        }
    }
}

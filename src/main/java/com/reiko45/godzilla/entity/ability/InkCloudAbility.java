package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.KaijuEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

/** Tiamat's phosphorus ink: a blinding cloud from her venom hood. Kaiju caught in it lose their aim. */
public class InkCloudAbility extends KaijuAbility {
    public InkCloudAbility() {
        super("ink_cloud", 10, 30, 280, "charge", "ink");
        this.minRange = 0;
        this.maxRange = 20;
        this.useChance = 0.05f;
    }

    @Override
    public void activate(KaijuEntity kaiju, @Nullable LivingEntity target) {
        kaiju.level().playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), SoundEvents.SQUID_SQUIRT, SoundSource.HOSTILE, 5f, 0.4f);
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (!(kaiju.level() instanceof ServerLevel level) || tick % 5 != 0) return;
        double r = 8 * kaiju.getScale() + kaiju.getBbWidth();
        level.sendParticles(ParticleTypes.SQUID_INK, kaiju.getX(), kaiju.getY(0.5), kaiju.getZ(), 80, r * 0.5, kaiju.getBbHeight() * 0.5, r * 0.5, 0.05);
        level.sendParticles(ParticleTypes.GLOW, kaiju.getX(), kaiju.getY(0.5), kaiju.getZ(), 20, r * 0.5, kaiju.getBbHeight() * 0.5, r * 0.5, 0.02);
        for (LivingEntity e : AreaAbility.victims(kaiju, r)) {
            if (e instanceof KaijuEntity k) {
                if (k.isAbilityActive() && tick == 0) k.stopAbility();
                continue;
            }
            e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
            e.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
        }
    }
}

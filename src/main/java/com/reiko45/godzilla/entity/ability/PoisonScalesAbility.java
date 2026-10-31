package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.KaijuEntity;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

import javax.annotation.Nullable;

/** Mothra scatters poisonous scale powder over her enemy: a lingering cloud of Poison and Weakness. */
public class PoisonScalesAbility extends KaijuAbility {
    private static final DustParticleOptions SCALE_DUST = new DustParticleOptions(new Vector3f(1.0f, 0.85f, 0.35f), 1.5f);

    public PoisonScalesAbility() {
        super("poison_scales", 15, 60, 300, "charge", "scales");
        this.minRange = 0;
        this.maxRange = 36;
        this.useChance = 0.03f;
    }

    @Override
    public void activate(KaijuEntity kaiju, @Nullable LivingEntity target) {
        if (target == null) return;
        AreaEffectCloud cloud = new AreaEffectCloud(kaiju.level(), target.getX(), target.getY(), target.getZ());
        cloud.setOwner(kaiju);
        cloud.setRadius(5.0f * kaiju.getScale() + target.getBbWidth());
        cloud.setRadiusPerTick(-0.01f);
        cloud.setDuration(200);
        cloud.setWaitTime(5);
        cloud.setParticle(SCALE_DUST);
        cloud.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1));
        cloud.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1));
        kaiju.level().addFreshEntity(cloud);
        if (target instanceof KaijuEntity) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 2));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1));
        }
        kaiju.level().playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.HOSTILE, 4f, 1.4f);
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (target != null) kaiju.getLookControl().setLookAt(target, 10f, 10f);
        if (kaiju.level() instanceof ServerLevel level && tick % 2 == 0) {
            level.sendParticles(SCALE_DUST, kaiju.getX(), kaiju.getY(0.5), kaiju.getZ(), 30,
                    kaiju.getBbWidth() * 1.5, kaiju.getBbHeight() * 0.3, kaiju.getBbWidth() * 1.5, 0.0);
        }
    }
}

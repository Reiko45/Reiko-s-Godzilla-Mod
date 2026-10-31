package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.KaijuEntity;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

import javax.annotation.Nullable;

/** Biollante's rose releases a cloud of spores: poison for her enemies, healing for her. */
public class SporeBurstAbility extends KaijuAbility {
    public SporeBurstAbility() {
        super("spore_burst", 20, 40, 400, "charge", "spores");
        this.minRange = 0;
        this.maxRange = 20;
        this.useChance = 0.02f;
    }

    @Override
    protected boolean extraStartCheck(KaijuEntity kaiju, LivingEntity target) {
        return kaiju.getHealth() < kaiju.getMaxHealth() * 0.9f;
    }

    @Override
    public void activate(KaijuEntity kaiju, @Nullable LivingEntity target) {
        if (!(kaiju.level() instanceof ServerLevel level)) return;
        kaiju.heal(kaiju.getMaxHealth() * 0.1f);
        AreaEffectCloud cloud = new AreaEffectCloud(level, kaiju.getX(), kaiju.getY(), kaiju.getZ());
        cloud.setOwner(kaiju);
        cloud.setRadius(8f * kaiju.getScale());
        cloud.setDuration(160);
        cloud.setRadiusPerTick(-0.01f);
        cloud.setParticle(new DustParticleOptions(new Vector3f(0.6f, 0.9f, 0.3f), 1.5f));
        cloud.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
        level.addFreshEntity(cloud);
        level.playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), SoundEvents.SPORE_BLOSSOM_BREAK, SoundSource.HOSTILE, 4f, 0.5f);
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (kaiju.level() instanceof ServerLevel level && tick % 2 == 0) {
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, kaiju.getX(), kaiju.getY(0.8), kaiju.getZ(), 20,
                    kaiju.getBbWidth() * 0.6, kaiju.getBbHeight() * 0.2, kaiju.getBbWidth() * 0.6, 0.05);
        }
    }
}

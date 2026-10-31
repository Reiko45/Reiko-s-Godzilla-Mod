package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.KaijuEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/** Biollante whips her vines in a wide circle, then drags whatever she hit toward her. */
public class VineLashAbility extends KaijuAbility {
    public VineLashAbility() {
        super("vine_lash", 12, 20, 120, "charge", "lash");
        this.minRange = 0;
        this.maxRange = 14;
        this.useChance = 0.06f;
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (tick != 8 || !(kaiju.level() instanceof ServerLevel level)) return;
        double radius = maxRange * kaiju.getScale();
        float damage = (float) kaiju.getAttributeValue(Attributes.ATTACK_DAMAGE);
        level.playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 5f, 0.4f);
        for (LivingEntity e : AreaAbility.victims(kaiju, radius)) {
            if (e.hurt(kaiju.damageSources().mobAttack(kaiju), damage)) {
                Vec3 pull = kaiju.position().subtract(e.position()).multiply(1, 0, 1).normalize();
                e.push(pull.x * 1.2, 0.5, pull.z * 1.2);
                e.hurtMarked = true;
            }
            level.sendParticles(ParticleTypes.COMPOSTER, e.getX(), e.getY(0.5), e.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
        }
    }
}

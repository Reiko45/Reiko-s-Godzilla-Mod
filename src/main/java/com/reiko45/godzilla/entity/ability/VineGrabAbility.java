package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.KaijuEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Biollante's toothed vine maws seize one victim, reel it in and chew on it. Kaiju are held in place instead of
 * pulled.
 */
public class VineGrabAbility extends KaijuAbility {
    @Nullable
    private LivingEntity held;

    public VineGrabAbility() {
        this("vine_grab");
    }

    /** Any grab-and-reel attack: Scylla's tendrils, the Skull Devil's tongue. */
    public VineGrabAbility(String name) {
        super(name, 10, 60, 200, "charge", "grab");
        this.minRange = 4;
        this.maxRange = 24;
        this.useChance = 0.04f;
    }

    @Override
    public void activate(KaijuEntity kaiju, @Nullable LivingEntity target) {
        held = target;
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (held == null || !held.isAlive() || !(kaiju.level() instanceof ServerLevel level)) {
            kaiju.requestAbilityEnd();
            return;
        }
        Vec3 mouth = kaiju.getBeamOrigin(0, 1f);
        if (held instanceof KaijuEntity) {
            held.setDeltaMovement(held.getDeltaMovement().multiply(0.2, 1, 0.2));
        } else {
            Vec3 pull = mouth.subtract(held.position()).scale(0.08);
            held.setDeltaMovement(pull);
            held.hurtMarked = true;
        }
        if (tick % 10 == 0) {
            held.hurt(kaiju.damageSources().mobAttack(kaiju), (float) kaiju.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5f);
        }
        if (tick % 2 == 0) {
            for (int i = 0; i <= 10; i++) {
                Vec3 p = mouth.lerp(held.getBoundingBox().getCenter(), i / 10.0);
                level.sendParticles(ParticleTypes.COMPOSTER, p.x, p.y, p.z, 1, 0.1, 0.1, 0.1, 0);
            }
        }
    }

    @Override
    public void stop(KaijuEntity kaiju) {
        held = null;
    }
}

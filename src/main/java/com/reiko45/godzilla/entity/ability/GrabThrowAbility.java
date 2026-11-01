package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.KaijuEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Kong's wrestling: seize the target, lift it, and hurl it away (in Rio he threw the Skar King straight to Godzilla).
 * Anything much bigger than the thrower only stumbles.
 */
public class GrabThrowAbility extends KaijuAbility {
    private static final int HOLD = 16;
    @Nullable
    private LivingEntity held;

    public GrabThrowAbility() {
        super("grab_throw", 6, 26, 220, "charge", "throw");
        this.minRange = 0;
        this.maxRange = 5;
        this.useChance = 0.05f;
    }

    @Override
    public void activate(KaijuEntity kaiju, @Nullable LivingEntity target) {
        held = target != null && kaiju.distanceTo(target) < kaiju.getMeleeReach(target) + 3 * kaiju.getScale() ? target : null;
        if (held == null) kaiju.requestAbilityEnd();
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (held == null || !held.isAlive() || !(kaiju.level() instanceof ServerLevel)) {
            kaiju.requestAbilityEnd();
            return;
        }
        double mass = Math.max(0.25, Math.min(1.0, kaiju.getBbHeight() / Math.max(0.5, held.getBbHeight()) * 0.8));
        Vec3 forward = Vec3.directionFromRotation(0, kaiju.yBodyRot);
        if (tick < HOLD) {
            // Hoist it up in front.
            Vec3 hold = kaiju.position().add(forward.scale(kaiju.getBbWidth() * 0.7 + held.getBbWidth() * 0.5)).add(0, kaiju.getBbHeight() * 0.5 * mass, 0);
            Vec3 pull = hold.subtract(held.position()).scale(0.35 * mass);
            held.setDeltaMovement(pull);
            held.hurtMarked = true;
            held.fallDistance = 0;
        } else if (tick == HOLD) {
            float damage = (float) kaiju.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.3f;
            held.hurt(kaiju.damageSources().mobAttack(kaiju), damage);
            Vec3 throwDir = forward.scale(2.4 * mass * Math.max(1.0, Math.sqrt(kaiju.getScale()))).add(0, 0.9 * mass, 0);
            held.setDeltaMovement(throwDir);
            held.hurtMarked = true;
            kaiju.level().playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.HOSTILE, 4f, 0.5f);
            held = null;
        }
    }

    @Override
    public void stop(KaijuEntity kaiju) {
        held = null;
    }
}

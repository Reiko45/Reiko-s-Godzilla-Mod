package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * A Warbat's attack (Godzilla vs. Kong): swoop on something small enough to carry, haul it high into the air and let
 * go. Machines (a HEAV, a jet) get torn apart instead.
 */
public class SnatchAbility extends KaijuAbility {
    @Nullable
    private LivingEntity held;
    private Vec3 direction = Vec3.ZERO;

    public SnatchAbility() {
        super("snatch", 10, 70, 240, "charge", "dive");
        this.minRange = 4;
        this.maxRange = 48;
        this.useChance = 0.05f;
    }

    @Override
    protected boolean extraStartCheck(KaijuEntity kaiju, LivingEntity target) {
        return kaiju instanceof FlyingKaijuEntity f && f.isFlying() && target.getBbHeight() < kaiju.getBbHeight() * 1.2 && kaiju.hasLineOfSight(target);
    }

    @Override
    public void activate(KaijuEntity kaiju, @Nullable LivingEntity target) {
        held = null;
        Vec3 aim = target != null ? target.getBoundingBox().getCenter() : kaiju.position().add(kaiju.getLookAngle().scale(20));
        direction = aim.subtract(kaiju.position()).normalize();
        kaiju.level().playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), SoundEvents.PHANTOM_SWOOP, SoundSource.HOSTILE, 5f, 0.5f);
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (!(kaiju.level() instanceof ServerLevel)) return;
        double speed = 1.4 * Math.max(1.0, Math.sqrt(kaiju.getScale()));
        if (held == null) {
            if (target != null && target.isAlive()) direction = target.getBoundingBox().getCenter().subtract(kaiju.position()).normalize();
            kaiju.setDeltaMovement(direction.scale(speed));
            if (target != null && target.isAlive() && kaiju.getBoundingBox().inflate(1.5 * kaiju.getScale()).intersects(target.getBoundingBox())) {
                float damage = (float) kaiju.getAttributeValue(Attributes.ATTACK_DAMAGE);
                boolean machine = target instanceof KaijuEntity k && k.getFaction() == KaijuFaction.HUMAN;
                target.hurt(kaiju.damageSources().mobAttack(kaiju), machine ? damage * 3 : damage);
                if (target instanceof KaijuEntity && !machine) {
                    kaiju.requestAbilityEnd();
                    return;
                }
                held = target;
            } else if (tick > 40) {
                kaiju.requestAbilityEnd();
            }
            return;
        }
        // Climb with the prey, then drop it.
        kaiju.setDeltaMovement(new Vec3(kaiju.getDeltaMovement().x * 0.8, 0.6, kaiju.getDeltaMovement().z * 0.8));
        Vec3 claws = kaiju.position().add(0, -held.getBbHeight() - 0.5, 0);
        held.setDeltaMovement(claws.subtract(held.position()).scale(0.5));
        held.hurtMarked = true;
        held.fallDistance = 0;
        if (tick >= 60 || !held.isAlive()) {
            held.setDeltaMovement(kaiju.getDeltaMovement().multiply(1, 0, 1));
            held.hurtMarked = true;
            held = null;
            kaiju.requestAbilityEnd();
        }
    }

    @Override
    public void stop(KaijuEntity kaiju) {
        held = null;
    }
}

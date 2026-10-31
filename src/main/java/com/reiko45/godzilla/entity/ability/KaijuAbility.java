package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.KaijuEntity;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.animation.RawAnimation;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * A special attack with a charge-up phase (the telegraph players can read) and an active phase.
 * Abilities are created in the kaiju's constructor on both sides so the client can pick the matching animation.
 */
public abstract class KaijuAbility {
    public final String name;
    public final int chargeTicks;
    public final int activeTicks;
    public final int cooldownTicks;

    protected double minRange = 0;
    protected double maxRange = 32;
    protected float useChance = 0.05f;
    private Predicate<KaijuEntity> condition = k -> true;
    private int cooldown;

    public final RawAnimation chargeAnimation;
    public final RawAnimation activeAnimation;

    protected KaijuAbility(String name, int chargeTicks, int activeTicks, int cooldownTicks, String chargeAnim, String activeAnim) {
        this.name = name;
        this.chargeTicks = chargeTicks;
        this.activeTicks = activeTicks;
        this.cooldownTicks = cooldownTicks;
        this.chargeAnimation = RawAnimation.begin().thenLoop(chargeAnim);
        this.activeAnimation = RawAnimation.begin().thenLoop(activeAnim);
        this.cooldown = cooldownTicks / 3;
    }

    /** Range in blocks at scale 1; scaled by the kaiju's size when checked. */
    public KaijuAbility range(double min, double max) {
        this.minRange = min;
        this.maxRange = max;
        return this;
    }

    public KaijuAbility chance(float perTick) {
        this.useChance = perTick;
        return this;
    }

    public KaijuAbility when(Predicate<KaijuEntity> condition) {
        this.condition = condition;
        return this;
    }

    public float useChance() {
        return useChance;
    }

    public boolean isReady(KaijuEntity kaiju) {
        return cooldown <= 0 && condition.test(kaiju);
    }

    public boolean canStart(KaijuEntity kaiju, LivingEntity target) {
        if (!isReady(kaiju)) return false;
        double scale = Math.max(0.5, kaiju.getScale());
        double dist = kaiju.distanceTo(target) - (kaiju.getBbWidth() + target.getBbWidth()) / 2.0;
        return dist >= minRange * scale && dist <= maxRange * scale && extraStartCheck(kaiju, target);
    }

    protected boolean extraStartCheck(KaijuEntity kaiju, LivingEntity target) {
        return true;
    }

    public void start(KaijuEntity kaiju, @Nullable LivingEntity target) {}

    public void tickCharge(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {}

    /** Called once when the charge finishes. */
    public void activate(KaijuEntity kaiju, @Nullable LivingEntity target) {}

    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {}

    public void stop(KaijuEntity kaiju) {}

    public void tickCooldown() {
        if (cooldown > 0) cooldown--;
    }

    public void resetCooldown() {
        cooldown = cooldownTicks;
    }

    /** Where to aim: the pilot's crosshair, else the target, else straight ahead. */
    protected static net.minecraft.world.phys.Vec3 aimPoint(KaijuEntity kaiju, @Nullable LivingEntity target) {
        net.minecraft.world.phys.Vec3 pilot = kaiju.getPilotAimPoint();
        if (pilot != null) return pilot;
        if (target != null) return target.getBoundingBox().getCenter();
        return kaiju.getEyePosition().add(net.minecraft.world.phys.Vec3.directionFromRotation(0, kaiju.yBodyRot).scale(24));
    }

    /** Helper: turn the kaiju's body toward a point, at most {@code maxDegrees} per tick. */
    protected static void faceTowards(KaijuEntity kaiju, double x, double z, float maxDegrees) {
        float target = (float) (Math.atan2(z - kaiju.getZ(), x - kaiju.getX()) * (180.0 / Math.PI)) - 90.0f;
        float yaw = net.minecraft.util.Mth.approachDegrees(kaiju.getYRot(), target, maxDegrees);
        kaiju.setYRot(yaw);
        kaiju.yBodyRot = yaw;
    }
}

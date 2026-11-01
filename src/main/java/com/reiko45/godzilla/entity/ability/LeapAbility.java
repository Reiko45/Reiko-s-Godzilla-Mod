package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * A leaping attack that comes down on the target (Kong's jumps from building to building, the Skar King's pounce,
 * Apex Mechagodzilla's thruster boost). Landing slams everything around the point of impact.
 */
public class LeapAbility extends KaijuAbility {
    private final double power;
    private final float damageMultiplier;
    private boolean airborne;

    public LeapAbility(String name, double power, float damageMultiplier, int cooldown) {
        super(name, 12, 40, cooldown, "charge", "leap");
        this.power = power;
        this.damageMultiplier = damageMultiplier;
        this.minRange = 8;
        this.maxRange = 36;
        this.useChance = 0.04f;
    }

    @Override
    public void tickCharge(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (target != null) faceTowards(kaiju, target.getX(), target.getZ(), 20f);
    }

    @Override
    public void activate(KaijuEntity kaiju, @Nullable LivingEntity target) {
        Vec3 aim = aimPoint(kaiju, target);
        Vec3 flat = aim.subtract(kaiju.position()).multiply(1, 0, 1);
        double dist = Math.max(1.0, flat.length());
        Vec3 dir = flat.normalize();
        double scale = Math.max(1.0, Math.sqrt(kaiju.getScale()));
        double horizontal = Math.min(power * scale, 0.09 * dist + 0.4);
        kaiju.setDeltaMovement(dir.x * horizontal, 0.9 * Math.sqrt(scale) * power * 0.7, dir.z * horizontal);
        kaiju.hasImpulse = true;
        airborne = false;
        kaiju.level().playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 3f, 0.6f);
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (!kaiju.onGround()) airborne = true;
        if (tick > 3 && (kaiju.onGround() || kaiju.isInWater()) && airborne) {
            slam(kaiju);
            kaiju.requestAbilityEnd();
        }
    }

    private void slam(KaijuEntity kaiju) {
        if (!(kaiju.level() instanceof ServerLevel level)) return;
        double radius = 5 * kaiju.getScale() + kaiju.getBbWidth() * 0.5;
        float damage = (float) kaiju.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;
        level.playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 5f, 0.5f);
        level.sendParticles(ParticleTypes.EXPLOSION, kaiju.getX(), kaiju.getY() + 0.5, kaiju.getZ(), 8, radius * 0.4, 0.5, radius * 0.4, 0);
        for (LivingEntity e : AreaAbility.victims(kaiju, radius)) {
            if (e.hurt(kaiju.damageSources().mobAttack(kaiju), damage)) {
                Vec3 away = e.position().subtract(kaiju.position()).multiply(1, 0, 1).normalize();
                e.push(away.x * 1.5, 0.6, away.z * 1.5);
            }
        }
        if (KaijuDestruction.canGrief(level, kaiju)) {
            KaijuDestruction.breakSphere(level, kaiju.position(), (float) Math.min(6, 1.5 * kaiju.getScale()),
                    GodzillaConfig.get(GodzillaConfig.MAX_BLOCKS_PER_TICK, 48), false, kaiju.getRandom());
        }
    }

    @Override
    public void stop(KaijuEntity kaiju) {
        airborne = false;
    }
}

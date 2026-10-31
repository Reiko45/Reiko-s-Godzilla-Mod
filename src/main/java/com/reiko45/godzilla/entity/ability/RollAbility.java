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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/** Anguirus's "Thunderball": curls into a spiked ball and rolls through everything in its way. */
public class RollAbility extends KaijuAbility {
    private Vec3 direction = Vec3.ZERO;

    public RollAbility() {
        super("thunderball", 15, 30, 160, "curl", "roll");
        this.minRange = 6;
        this.maxRange = 40;
        this.useChance = 0.04f;
    }

    @Override
    public void tickCharge(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (target != null) KaijuAbility.faceTowards(kaiju, target.getX(), target.getZ(), 15f);
    }

    @Override
    public void activate(KaijuEntity kaiju, @Nullable LivingEntity target) {
        Vec3 aim = target != null ? target.position() : kaiju.position().add(Vec3.directionFromRotation(0, kaiju.getYRot()).scale(20));
        direction = aim.subtract(kaiju.position()).multiply(1, 0, 1).normalize();
        if (direction.lengthSqr() < 1.0E-4) direction = Vec3.directionFromRotation(0, kaiju.getYRot());
        kaiju.level().playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 4f, 0.7f);
    }

    /** Lets a rider aim the roll along their view. */
    public void setDirection(Vec3 direction) {
        this.direction = direction.multiply(1, 0, 1).normalize();
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        double speed = 1.1 * Math.max(1.0, Math.sqrt(kaiju.getScale()));
        kaiju.setDeltaMovement(direction.x * speed, kaiju.getDeltaMovement().y, direction.z * speed);
        if (!(kaiju.level() instanceof ServerLevel level) || tick % 2 != 0) return;
        AABB box = kaiju.getBoundingBox().inflate(1.0);
        float damage = (float) kaiju.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.2f;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, e -> AreaAbility.isVictim(kaiju, e))) {
            if (e.hurt(kaiju.damageSources().mobAttack(kaiju), damage)) e.push(direction.x * 2.5, 0.7, direction.z * 2.5);
        }
        level.sendParticles(ParticleTypes.CRIT, kaiju.getX(), kaiju.getY(0.5), kaiju.getZ(), 10, kaiju.getBbWidth() * 0.4, kaiju.getBbHeight() * 0.3, kaiju.getBbWidth() * 0.4, 0.3);
        if (KaijuDestruction.canGrief(level, kaiju)) {
            AABB ahead = kaiju.getBoundingBox().move(direction.scale(1.0));
            ahead = new AABB(ahead.minX, kaiju.getY() + 0.6, ahead.minZ, ahead.maxX, ahead.maxY, ahead.maxZ);
            KaijuDestruction.breakBox(level, ahead, GodzillaConfig.get(GodzillaConfig.MAX_BLOCKS_PER_TICK, 48));
        }
    }
}

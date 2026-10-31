package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Rodan's supersonic dive: rockets through the target, and the sonic boom in its wake flattens everything along
 * the path.
 */
public class DiveAbility extends KaijuAbility {
    private Vec3 direction = Vec3.ZERO;
    private final float speed;

    public DiveAbility(float speed) {
        super("sonic_dive", 15, 30, 200, "charge", "dive");
        this.speed = speed;
        this.minRange = 10;
        this.maxRange = 64;
        this.useChance = 0.04f;
    }

    @Override
    protected boolean extraStartCheck(KaijuEntity kaiju, LivingEntity target) {
        return kaiju instanceof FlyingKaijuEntity f && f.isFlying() && kaiju.hasLineOfSight(target);
    }

    @Override
    public void tickCharge(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        kaiju.setDeltaMovement(kaiju.getDeltaMovement().scale(0.7).add(0, 0.08, 0));
        if (target != null) {
            KaijuAbility.faceTowards(kaiju, target.getX(), target.getZ(), 12f);
            kaiju.getLookControl().setLookAt(target, 30f, 30f);
        }
    }

    @Override
    public void activate(KaijuEntity kaiju, @Nullable LivingEntity target) {
        Vec3 aim = target != null ? target.position().add(0, target.getBbHeight() * 0.5, 0) : kaiju.position().add(kaiju.getLookAngle().scale(20));
        direction = aim.subtract(kaiju.position()).normalize();
        kaiju.level().playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 6f, 0.8f);
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        Vec3 velocity = direction.scale(speed * Math.max(1.0, Math.sqrt(kaiju.getScale())));
        // Pull out of the dive before hitting the ground.
        if (kaiju.getY() - kaiju.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, kaiju.getBlockX(), kaiju.getBlockZ()) < 3) {
            direction = new Vec3(direction.x, Math.abs(direction.y) * 0.5 + 0.2, direction.z).normalize();
        }
        kaiju.setDeltaMovement(velocity);
        kaiju.setYRot((float) (Math.atan2(direction.z, direction.x) * (180.0 / Math.PI)) - 90.0f);
        kaiju.yBodyRot = kaiju.getYRot();
        if (!(kaiju.level() instanceof ServerLevel level)) return;
        AABB swept = kaiju.getBoundingBox().inflate(2.0 * kaiju.getScale());
        if (tick % 2 == 0) {
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, swept, e -> AreaAbility.isVictim(kaiju, e))) {
                KaijuDestruction.hurtIgnoringCooldown(e, ModDamageTypes.source(level, ModDamageTypes.SONIC_BOOM, kaiju), 18f);
                e.push(velocity.x * 0.6, 0.8, velocity.z * 0.6);
            }
            level.sendParticles(ParticleTypes.SONIC_BOOM, kaiju.getX(), kaiju.getY(0.5), kaiju.getZ(), 1, 0, 0, 0, 0);
            if (KaijuDestruction.canGrief(level, kaiju)) {
                KaijuDestruction.breakBox(level, swept.deflate(1.0 * kaiju.getScale()), GodzillaConfig.get(GodzillaConfig.MAX_BLOCKS_PER_TICK, 48));
            }
        }
    }

    @Override
    public void stop(KaijuEntity kaiju) {
        kaiju.setDeltaMovement(kaiju.getDeltaMovement().scale(0.3).add(0, 0.6, 0));
    }
}

package com.reiko45.godzilla.entity.millennium;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Zilla (Godzilla Final Wars, 2004), a Xilien weapon sent against Sydney. Blindingly fast and agile, leaping huge
 * distances, and often jumping clear of a beam aimed at it. Fragile, though: Godzilla dealt with it in seconds.
 */
public class ZillaEntity extends KaijuEntity {
    public ZillaEntity(EntityType<? extends ZillaEntity> type, Level level) {
        super(type, level);
        this.xpReward = 250;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(350, 8, 2, 22, 0.38);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    public boolean hasMBase() {
        return true;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new LeapAbility("pounce", 2.0, 1.4f, 100));
        list.add(new HoldAbility("jaws", "grab", HoldAbility.Mode.MOUTH, 24, 6, 0.4f, 0f, 160));
        list.add(new TailSwipeAbility());
        list.add(new RoarAbility(ModSounds.ZILLA_ROAR));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 1.8f, 1.6f, new Vec3(0, 8.0, 3.2), 1.3f),
                new KaijuPart(this, "tail", 1.8f, 1.6f, new Vec3(0, 3.0, -4.6), 0.8f)
        };
    }

    @Override
    protected double variantScale() {
        return 1.9;
    }

    /** Dodges: half the time it leaps clear of a beam before it lands. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide && isBeam(source) && onGround() && !isAbilityActive() && random.nextBoolean()) {
            Vec3 side = Vec3.directionFromRotation(0, yBodyRot + (random.nextBoolean() ? 90 : -90));
            setDeltaMovement(side.x * 1.6, 0.9, side.z * 1.6);
            hasImpulse = true;
            if (level() instanceof ServerLevel server) server.sendParticles(ParticleTypes.CLOUD, getX(), getY(), getZ(), 16, 1, 0.2, 1, 0.05);
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ZILLA_ROAR.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ZILLA_ROAR.get();
    }
}

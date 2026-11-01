package com.reiko45.godzilla.entity.showa;

import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.entity.projectile.KaijuMissileEntity;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.world.AlienSignal;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Megalon, the beetle god of Seatopia (Godzilla vs. Megalon, 1973). Drill arms that join into a Double Drill to
 * tunnel under his enemies, napalm bombs spat from the mouth, yellow lightning from the horn, grasshopper leaps, and
 * a ring of fire to trap his prey.
 * <p>
 * He's not bright: without Seatopia's guidance he thrashes around at whatever is right in front of him. Under the
 * Seatopian signal he hunts properly.
 */
public class MegalonEntity extends KaijuEntity {
    public MegalonEntity(EntityType<? extends MegalonEntity> type, Level level) {
        super(type, level);
        this.xpReward = 450;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(850, 26, 12, 28, 0.23);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    public boolean controllableBy(AlienSignal signal) {
        return signal == AlienSignal.SEATOPIA || super.controllableBy(signal);
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new HoldAbility("drill_arms", "drill", HoldAbility.Mode.FRONT, 30, 5, 0.35f, 0f, 200).damageType(ModDamageTypes.BUZZSAW));
        list.add(new BurrowAbility("double_drill", 0.9, 1.6f, 500));
        list.add(new SprayAbility("napalm", k -> new KaijuMissileEntity(k.level(), k, KaijuMissileEntity.Payload.EXPLOSIVE),
                ModSounds.MEGALON_SCREECH, 0.9f, 5f, 40, 260).interval(10));
        list.add(new BeamAbility("beast_killer_beam", BeamStyle.ELECTRIC, 1, 0.35f, ModDamageTypes.BEAST_KILLER, 20, 35, 200)
                .damage(9f, 3).blocks(1.2f, true).chargeParticle(ParticleTypes.ELECTRIC_SPARK).range(4, 40));
        list.add(new LeapAbility("grasshopper_leap", 1.4, 1.3f, 240));
        list.add(new FireRingAbility());
        list.add(new RoarAbility(ModSounds.MEGALON_SCREECH));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{new KaijuPart(this, "head", 1.8f, 2.2f, new Vec3(0, 8.3, 1.8), 1.3f)};
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 10.4, 1.8);
    }

    @Override
    protected double variantScale() {
        return 1.15;
    }

    /** Without guidance Megalon only notices what's right in front of him. */
    @Override
    public boolean wantsToAttack(LivingEntity other) {
        if (!isAlienControlled() && getTarget() == null && distanceTo(other) > 20 + getBbWidth()) return false;
        return super.wantsToAttack(other);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.MEGALON_SCREECH.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MEGALON_SCREECH.get();
    }
}

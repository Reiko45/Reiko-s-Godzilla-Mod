package com.reiko45.godzilla.entity.showa;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.entity.projectile.WebShotEntity;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.world.AlienSignal;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Kumonga, the giant spider of Sollgel Island (Son of Godzilla, 1967). It waits in its valley den, ambushes from
 * underground, spits sticky thread that binds its prey, and stabs with a venom needle. Fire is its undoing: Godzilla
 * and Minilla burned it to death. The Final Wars version is bigger and carries M-Base.
 */
public class KumongaEntity extends KaijuEntity {
    public static final int SHOWA = 0;
    public static final int FINAL_WARS = 1;

    public KumongaEntity(EntityType<? extends KumongaEntity> type, Level level) {
        super(type, level);
        this.xpReward = 300;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(450, 14, 6, 20, 0.27);
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
    public boolean controllableBy(AlienSignal signal) {
        return signal == AlienSignal.KILAAK || super.controllableBy(signal);
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new SprayAbility("web_thread", k -> new WebShotEntity(k.level(), k, false), ModSounds.KUMONGA_HISS, 1.2f, 3f, 32, 220).interval(8));
        list.add(new SprayAbility("venom_needle", k -> new WebShotEntity(k.level(), k, true), ModSounds.KUMONGA_HISS, 1.8f, 1f, 12, 180).interval(6));
        list.add(new BurrowAbility("ambush", 0.8, 1.4f, 600));
        list.add(new LeapAbility("pounce", 1.2, 1.2f, 220));
        list.add(new HoldAbility("bite", "grab", HoldAbility.Mode.FRONT, 30, 6, 0.4f, 0.2f, 200).damageType(ModDamageTypes.ENERGY_DRAIN));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 2.0f, 1.8f, new Vec3(0, 2.0, 3.8), 1.3f),
                new KaijuPart(this, "abdomen", 3.4f, 2.8f, new Vec3(0, 2.2, -3.6), 0.9f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 2.2, 4.6);
    }

    @Override
    protected double variantScale() {
        return getVariant() == FINAL_WARS ? 1.35 : 1.0;
    }

    @Override
    protected void applyVariantAttributes() {
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(getVariant() == FINAL_WARS ? 800 : 450);
        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(getVariant() == FINAL_WARS ? 26 : 20);
    }

    @Override
    public Component getTypeName() {
        return Component.translatable(getVariant() == FINAL_WARS ? "entity.reikogodzilla.kumonga_final_wars" : "entity.reikogodzilla.kumonga");
    }

    /** Fire and atomic breath burn it badly. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FIRE) || source.is(ModDamageTypes.ATOMIC_BREATH)) amount *= 2.0f;
        return super.hurt(source, amount);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.KUMONGA_HISS.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SPIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }

    @Override
    protected void playStepSound(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        playSound(SoundEvents.SPIDER_STEP, 2.0f, 0.4f);
    }
}

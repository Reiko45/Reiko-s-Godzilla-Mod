package com.reiko45.godzilla.entity.showa;

import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.world.AlienSignal;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Kamacuras (Son of Godzilla, 1967): ordinary mantises on Sollgel Island blown up to giant size by a failed weather
 * experiment. They hunt in threes, slash with scythe forelegs, and fly in short bursts. They burn very easily.
 * They're drawn to a hatching Minilla egg. The Final Wars Kamacuras is almost twice the size and carries M-Base.
 */
public class KamacurasEntity extends FlyingKaijuEntity {
    public static final int SHOWA = 0;
    public static final int FINAL_WARS = 1;

    public KamacurasEntity(EntityType<? extends KamacurasEntity> type, Level level) {
        super(type, level);
        this.xpReward = 150;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(220, 8, 2, 16, 0.3).add(Attributes.FLYING_SPEED, 0.7);
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
        list.add(new HoldAbility("piton_crush", "grab", HoldAbility.Mode.FRONT, 24, 6, 0.45f, 0f, 180).damageType(ModDamageTypes.BUZZSAW));
        list.add(new LeapAbility("pounce", 1.3, 1.2f, 200));
        list.add(new DiveAbility(1.6f));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{new KaijuPart(this, "abdomen", 2.2f, 2.0f, new Vec3(0, 1.8, -3.4), 0.9f)};
    }

    @Override
    protected double variantScale() {
        return getVariant() == FINAL_WARS ? 1.8 : 1.0;
    }

    @Override
    protected void applyVariantAttributes() {
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(getVariant() == FINAL_WARS ? 450 : 220);
        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(getVariant() == FINAL_WARS ? 24 : 16);
    }

    @Override
    public Component getTypeName() {
        return Component.translatable(getVariant() == FINAL_WARS ? "entity.reikogodzilla.kamacuras_final_wars" : "entity.reikogodzilla.kamacuras");
    }

    /** Very flammable. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FIRE) || source.is(ModDamageTypes.ATOMIC_BREATH)) {
            amount *= 3.0f;
            igniteForSeconds(8);
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean showsBossBar() {
        return getVariant() == FINAL_WARS && super.showsBossBar();
    }

    @Override
    public int landChance() {
        return 120;
    }

    @Override
    protected float regenPerSecond() {
        return 0.2f;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.KAMACURAS_SCREECH.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAMACURAS_SCREECH.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.KAMACURAS_SCREECH.get();
    }
}

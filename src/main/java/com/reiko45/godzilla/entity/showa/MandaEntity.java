package com.reiko45.godzilla.entity.showa;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.world.AlienSignal;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Manda, the sea dragon (Atragon, 1963). Guardian of the undersea Mu Empire in the Showa films, a Monster Island
 * resident in Destroy All Monsters. It coils around its enemy, even a battleship, and crushes it.
 * <p>
 * Cold is its weakness: the Gotengo froze it solid with the Zero Cannon. Manda freezes twice as fast as other kaiju,
 * and a frozen Manda shatters under heavy blows. The Final Wars Manda is a 300-metre deep-sea monster.
 */
public class MandaEntity extends KaijuEntity {
    public static final int SHOWA = 0;
    public static final int FINAL_WARS = 1;

    public MandaEntity(EntityType<? extends MandaEntity> type, Level level) {
        super(type, level);
        this.xpReward = 400;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(600, 18, 8, 24, 0.26);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return getVariant() == FINAL_WARS ? KaijuFaction.INVADER : KaijuFaction.TITAN;
    }

    @Override
    public boolean hasMBase() {
        return getVariant() == FINAL_WARS;
    }

    @Override
    public boolean controllableBy(AlienSignal signal) {
        return signal == AlienSignal.KILAAK || super.controllableBy(signal);
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new HoldAbility("binding_breaker", "grab", HoldAbility.Mode.BODY, 80, 8, 0.5f, 0f, 300)
                .damageType(ModDamageTypes.CONSTRICTION).maxSize(3.0f).sound(ModSounds.MANDA_ROAR));
        list.add(new TailSwipeAbility());
        list.add(new RoarAbility(ModSounds.MANDA_ROAR));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "neck", 2.8f, 2.6f, new Vec3(0, 0.2, -5.0), 1.0f),
                new KaijuPart(this, "body", 2.8f, 2.6f, new Vec3(0, 0.2, -10.5), 0.9f),
                new KaijuPart(this, "body2", 2.4f, 2.2f, new Vec3(0, 0.2, -16.0), 0.8f),
                new KaijuPart(this, "tail", 2.0f, 1.8f, new Vec3(0, 0.2, -22.0), 0.7f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 1.6, 2.4);
    }

    @Override
    protected double variantScale() {
        return getVariant() == FINAL_WARS ? 1.6 : 1.0;
    }

    @Override
    protected void applyVariantAttributes() {
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(getVariant() == FINAL_WARS ? 1100 : 600);
        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(getVariant() == FINAL_WARS ? 32 : 24);
    }

    @Override
    public Component getTypeName() {
        return Component.translatable(getVariant() == FINAL_WARS ? "entity.reikogodzilla.manda_final_wars" : "entity.reikogodzilla.manda");
    }

    @Override
    public float freezeMultiplier() {
        return 2.0f;
    }

    /** Frozen solid, it shatters. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isFrozenSolid()) amount *= 2.0f;
        return super.hurt(source, amount);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.MANDA_ROAR.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MANDA_ROAR.get();
    }
}

package com.reiko45.godzilla.entity.kaiju;

import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.PoisonScalesAbility;
import com.reiko45.godzilla.entity.ability.WindGustAbility;
import com.reiko45.godzilla.entity.ai.GuardianDefendGoal;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Mothra (imago): the guardian of Infant Island. Defends players from hostile kaiju with clouds of poisonous scale
 * powder and hurricane wing beats.
 */
public class MothraEntity extends FlyingKaijuEntity {
    public MothraEntity(EntityType<? extends MothraEntity> type, Level level) {
        super(type, level);
        this.xpReward = 400;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(550, 10, 4, 16, 0.2).add(Attributes.FLYING_SPEED, 0.6);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.GUARDIAN;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(3, new GuardianDefendGoal(this));
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new PoisonScalesAbility());
        list.add(new WindGustAbility(3.0f));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "left_wing", 7.0f, 1.0f, new Vec3(-4.2, 1.6, 0), 0.6f),
                new KaijuPart(this, "right_wing", 7.0f, 1.0f, new Vec3(4.2, 1.6, 0), 0.6f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 2.1, 2.6);
    }

    @Override
    public int landChance() {
        return 1200;
    }

    @Override
    protected float regenPerSecond() {
        return 0.75f;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.MOTHRA_CRY.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.MOTHRA_CRY.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MOTHRA_CRY.get();
    }
}

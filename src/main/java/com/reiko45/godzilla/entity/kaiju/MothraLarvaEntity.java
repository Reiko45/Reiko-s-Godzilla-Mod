package com.reiko45.godzilla.entity.kaiju;

import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.LarvaKaijuEntity;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.SprayAbility;
import com.reiko45.godzilla.entity.ai.GuardianDefendGoal;
import com.reiko45.godzilla.entity.projectile.SilkShotEntity;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Mothra's larval form. Protects players, sprays binding silk, and once it has lived long enough out of combat it
 * spins a cocoon and emerges as the adult Mothra.
 */
public class MothraLarvaEntity extends LarvaKaijuEntity {
    public MothraLarvaEntity(EntityType<? extends MothraLarvaEntity> type, Level level) {
        super(type, level);
        this.xpReward = 200;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(400, 8, 4, 14, 0.17);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.GUARDIAN;
    }

    @Override
    protected EntityType<? extends FlyingKaijuEntity> adultType() {
        return ModEntities.MOTHRA.get();
    }

    @Override
    protected String emergeMessage() {
        return "message.reikogodzilla.mothra_emerges";
    }

    @Override
    protected void onEmerge(FlyingKaijuEntity adult) {
        adult.playSound(ModSounds.MOTHRA_CRY.get(), 8f, 1f);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(3, new GuardianDefendGoal(this));
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new SprayAbility("silk_spray", k -> new SilkShotEntity(k.level(), k), () -> SoundEvents.LLAMA_SPIT, 1.6f, 6f, 30, 140));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "body", 2.6f, 2.1f, new Vec3(0, 0, -3.0), 0.9f),
                new KaijuPart(this, "tail", 2.0f, 1.6f, new Vec3(0, 0, -5.6), 0.8f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 1.6, 1.9);
    }

    @Override
    protected double baseStepHeight() {
        return 1.5;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isCocoon() ? null : ModSounds.MOTHRA_CRY.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SILVERFISH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SILVERFISH_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 1.3f;
    }
}

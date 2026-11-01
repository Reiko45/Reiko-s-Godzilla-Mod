package com.reiko45.godzilla.entity.monsterverse;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.RoarAbility;
import com.reiko45.godzilla.entity.ability.RollAbility;
import com.reiko45.godzilla.entity.ability.VineGrabAbility;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.world.Radiation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Scylla, the spider-like cephalopod Titan (King of the Monsters; killed by Godzilla in Rome in Godzilla x Kong: The
 * New Empire). Scuttles fast, climbs anything, impales with her legs and seizes prey with her tendrils. She hunts
 * radiation, and answers King Ghidorah's Alpha Call.
 */
public class ScyllaEntity extends KaijuEntity {
    private static final EntityDataAccessor<Boolean> DATA_CLIMBING = SynchedEntityData.defineId(ScyllaEntity.class, EntityDataSerializers.BOOLEAN);

    public ScyllaEntity(EntityType<? extends ScyllaEntity> type, Level level) {
        super(type, level);
        this.xpReward = 450;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(850, 18, 8, 26, 0.32);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    public boolean followsAlpha() {
        return true;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new VineGrabAbility("tendril_grab"));
        list.add(new RollAbility("scuttle_charge", "charge", "walk", 1.3));
        list.add(new RoarAbility(ModSounds.SCYLLA_SCREECH));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{new KaijuPart(this, "head", 2.4f, 2.0f, new Vec3(0, 6.0, 3.2), 1.4f)};
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 6.0, 4.0);
    }

    @Override
    protected double variantScale() {
        return 2.0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CLIMBING, false);
    }

    @Override
    public boolean onClimbable() {
        return entityData.get(DATA_CLIMBING);
    }

    @Override
    public boolean canCrushBlocks() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) entityData.set(DATA_CLIMBING, horizontalCollision);
    }

    /** Her legs end in spikes. */
    @Override
    public void performMelee(LivingEntity target) {
        super.performMelee(target);
        target.hurt(damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.4f);
        if (!(target instanceof KaijuEntity)) target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel server)) return;
        if (tickCount % 40 == 0) {
            BlockPos source = Radiation.findNearest(server, position(), 6 * getScale() + getBbWidth());
            if (source != null && Radiation.drain(server, source, getEyePosition()) != null) heal(getMaxHealth() * 0.2f);
        }
        if (getTarget() == null && !isUnderAlphaCall() && tickCount % 120 == 0) {
            BlockPos source = Radiation.findNearest(server, position(), 160);
            if (source != null) setDestination(source);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.SCYLLA_SCREECH.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SCYLLA_SCREECH.get();
    }
}

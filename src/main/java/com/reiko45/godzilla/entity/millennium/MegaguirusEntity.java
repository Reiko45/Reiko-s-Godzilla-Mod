package com.reiko45.godzilla.entity.millennium;

import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Megaguirus (Godzilla vs. Megaguirus, 2000), queen of the Meganulon swarm. She flies at tremendous speed, drains
 * energy through the stinger in her tail, and once she's full she can turn that stolen power into a beam of her own.
 * Her wings beat out ultrasonic shockwaves that scramble electronics (they knocked the Dimension Tide out of orbit).
 * Godzilla beat her by catching her stinger: damage her tail enough and the stinger breaks off.
 */
public class MegaguirusEntity extends FlyingKaijuEntity {
    private static final EntityDataAccessor<Float> DATA_ENERGY = SynchedEntityData.defineId(MegaguirusEntity.class, EntityDataSerializers.FLOAT);
    private float stingerDamage;
    private boolean stingerBroken;

    public MegaguirusEntity(EntityType<? extends MegaguirusEntity> type, Level level) {
        super(type, level);
        this.xpReward = 500;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(900, 18, 8, 26, 0.26).add(Attributes.FLYING_SPEED, 0.95);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new HoldAbility("stinger_drain", "grab", HoldAbility.Mode.FRONT, 60, 6, 0.4f, 0.8f, 260)
                .damageType(ModDamageTypes.ENERGY_DRAIN).sound(ModSounds.MEGAGUIRUS_BUZZ)
                .onHit((k, e) -> ((MegaguirusEntity) k).addEnergy(e instanceof KaijuEntity g && g.isGodzilla() ? 18f : 10f))
                .when(k -> !((MegaguirusEntity) k).stingerBroken));
        list.add(new BeamAbility("stolen_breath", BeamStyle.ATOMIC, 1, 0.9f, ModDamageTypes.ATOMIC_BREATH, 30, 40, 200)
                .damage(16f, 4).blocks(2.0f, true).sounds(ModSounds.ATOMIC_BREATH_CHARGE, ModSounds.ATOMIC_BREATH_FIRE)
                .chargeParticle(ParticleTypes.SOUL_FIRE_FLAME).range(4, 48).when(k -> ((MegaguirusEntity) k).getEnergy() >= 100f));
        list.add(new SonicWaveAbility(14f));
        list.add(new DiveAbility(2.6f));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{new KaijuPart(this, "stinger", 1.6f, 1.6f, new Vec3(0, 1.6, -5.4), 1.5f)};
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 2.4, 3.2);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ENERGY, 0f);
    }

    public float getEnergy() {
        return entityData.get(DATA_ENERGY);
    }

    public void addEnergy(float amount) {
        entityData.set(DATA_ENERGY, Math.min(100f, getEnergy() + amount));
    }

    @Override
    public void startAbility(int index, LivingEntity target) {
        super.startAbility(index, target);
        if (!level().isClientSide && abilities.get(index).name.equals("stolen_breath")) entityData.set(DATA_ENERGY, 0f);
    }

    @Override
    public boolean hurtFromPart(KaijuPart part, DamageSource source, float amount) {
        if (part.name.equals("stinger") && !stingerBroken) {
            stingerDamage += amount * part.damageMultiplier;
            if (stingerDamage > getMaxHealth() * 0.12f) {
                stingerBroken = true;
                if (getActiveAbility() instanceof HoldAbility) stopAbility();
                playSound(net.minecraft.sounds.SoundEvents.TURTLE_EGG_CRACK, 6f, 0.5f);
                if (level() instanceof ServerLevel server) {
                    for (var p : server.players()) {
                        if (p.distanceToSqr(this) < 160 * 160) p.displayClientMessage(Component.translatable("message.reikogodzilla.megaguirus_stinger"), true);
                    }
                }
            }
        }
        return super.hurtFromPart(part, source, amount);
    }

    @Override
    public boolean prefersFlightInCombat() {
        return true;
    }

    @Override
    public int landChance() {
        return 600;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Energy", getEnergy());
        tag.putFloat("StingerDamage", stingerDamage);
        tag.putBoolean("StingerBroken", stingerBroken);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_ENERGY, tag.getFloat("Energy"));
        stingerDamage = tag.getFloat("StingerDamage");
        stingerBroken = tag.getBoolean("StingerBroken");
    }

    @Override
    public void playWingSound() {
        playSound(ModSounds.MEGAGUIRUS_BUZZ.get(), 3f, 1.0f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.MEGAGUIRUS_BUZZ.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MEGAGUIRUS_BUZZ.get();
    }
}

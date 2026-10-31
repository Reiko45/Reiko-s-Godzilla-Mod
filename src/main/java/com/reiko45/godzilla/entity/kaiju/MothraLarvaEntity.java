package com.reiko45.godzilla.entity.kaiju;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.SilkSprayAbility;
import com.reiko45.godzilla.entity.ai.GuardianDefendGoal;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.List;

/**
 * Mothra's larval form. Protects players, sprays binding silk, and once it has lived long enough out of combat it
 * spins a cocoon and emerges as the adult Mothra.
 */
public class MothraLarvaEntity extends KaijuEntity {
    private static final EntityDataAccessor<Boolean> DATA_COCOON = SynchedEntityData.defineId(MothraLarvaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final RawAnimation COCOON = RawAnimation.begin().thenLoop("cocoon");
    public static final int COCOON_TICKS = 1200;

    private int growth;
    private int cocoonTicks;

    public MothraLarvaEntity(EntityType<? extends MothraLarvaEntity> type, Level level) {
        super(type, level);
        this.xpReward = 200;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(400, 8, 4, 14, 0.17);
    }

    @Override
    public KaijuFaction getFaction() {
        return KaijuFaction.GUARDIAN;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(3, new GuardianDefendGoal(this));
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new SilkSprayAbility());
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
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_COCOON, false);
    }

    public boolean isCocoon() {
        return entityData.get(DATA_COCOON);
    }

    @Override
    public boolean canWander() {
        return !isCocoon();
    }

    @Override
    public boolean isImmobile() {
        return isCocoon() || super.isImmobile();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // The cocoon is tough silk: most damage is absorbed.
        return super.hurt(source, isCocoon() ? amount * 0.25f : amount);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (isCocoon()) {
            setTarget(null);
            if (++cocoonTicks >= COCOON_TICKS) emerge();
            else if (cocoonTicks % 20 == 0 && level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.END_ROD, getX(), getY(0.6), getZ(), 4, getBbWidth() * 0.5, getBbHeight() * 0.4, getBbWidth() * 0.5, 0.01);
            }
            return;
        }
        if (getTarget() == null) growth++;
        if (growth >= GodzillaConfig.get(GodzillaConfig.MOTHRA_GROWTH_TICKS, 12000) && onGround()) {
            entityData.set(DATA_COCOON, true);
            playSound(SoundEvents.WOOL_PLACE, 4f, 0.5f);
        }
    }

    private void emerge() {
        if (!(level() instanceof ServerLevel server)) return;
        MothraEntity mothra = ModEntities.MOTHRA.get().create(server);
        if (mothra == null) return;
        mothra.moveTo(getX(), getY() + 1, getZ(), getYRot(), 0);
        mothra.finalizeSpawn(server, server.getCurrentDifficultyAt(blockPosition()), MobSpawnType.CONVERSION, null);
        if (hasCustomName()) mothra.setCustomName(getCustomName());
        mothra.setFlying(true);
        server.addFreshEntity(mothra);
        server.sendParticles(ParticleTypes.FLASH, getX(), getY(0.5), getZ(), 1, 0, 0, 0, 0);
        server.sendParticles(ParticleTypes.END_ROD, getX(), getY(0.5), getZ(), 80, getBbWidth(), getBbHeight(), getBbWidth(), 0.1);
        mothra.playSound(ModSounds.MOTHRA_CRY.get(), 8f, 1f);
        for (var player : server.players()) {
            if (player.distanceToSqr(this) < 128 * 128) {
                player.displayClientMessage(Component.translatable("message.reikogodzilla.mothra_emerges"), true);
            }
        }
        discard();
    }

    @Override
    protected PlayState mainAnimation(AnimationState<KaijuEntity> state) {
        if (isCocoon()) return state.setAndContinue(COCOON);
        return super.mainAnimation(state);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Growth", growth);
        tag.putBoolean("Cocoon", isCocoon());
        tag.putInt("CocoonTicks", cocoonTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        growth = tag.getInt("Growth");
        entityData.set(DATA_COCOON, tag.getBoolean("Cocoon"));
        cocoonTicks = tag.getInt("CocoonTicks");
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

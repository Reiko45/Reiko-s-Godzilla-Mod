package com.reiko45.godzilla.entity;

import com.reiko45.godzilla.entity.ai.OwnerGoals;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

/** A kaiju that can belong to a player: sits, follows, and fights alongside its owner (Anguirus, Godzilla Junior). */
public abstract class TameableKaijuEntity extends KaijuEntity implements OwnableEntity {
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER = SynchedEntityData.defineId(TameableKaijuEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> DATA_SITTING = SynchedEntityData.defineId(TameableKaijuEntity.class, EntityDataSerializers.BOOLEAN);

    protected TameableKaijuEntity(EntityType<? extends TameableKaijuEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new OwnerGoals.Sit(this));
        this.goalSelector.addGoal(4, new OwnerGoals.FollowOwner(this));
        this.targetSelector.addGoal(1, new OwnerGoals.DefendOwner(this));
        this.targetSelector.addGoal(2, new OwnerGoals.AssistOwner(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_OWNER, Optional.empty());
        builder.define(DATA_SITTING, false);
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return entityData.get(DATA_OWNER).orElse(null);
    }

    public void setOwner(@Nullable Player player) {
        entityData.set(DATA_OWNER, player == null ? Optional.empty() : Optional.of(player.getUUID()));
    }

    public boolean isTame() {
        return getOwnerUUID() != null;
    }

    public boolean isOwnedBy(LivingEntity entity) {
        return entity.getUUID().equals(getOwnerUUID());
    }

    public boolean isSitting() {
        return entityData.get(DATA_SITTING);
    }

    public void setSitting(boolean sitting) {
        entityData.set(DATA_SITTING, sitting);
    }

    /** Toggle sit/follow. Returns the message shown to the owner. */
    protected void toggleSitting(Player player) {
        setSitting(!isSitting());
        if (!level().isClientSide) {
            player.displayClientMessage(Component.translatable(isSitting() ? "message.reikogodzilla.pet_sit" : "message.reikogodzilla.pet_follow",
                    getDisplayName()), true);
        }
    }

    /** Hearts (true) or smoke (false) over its head. */
    protected void showTamingResult(boolean success) {
        level().broadcastEntityEvent(this, success ? (byte) 7 : (byte) 6);
    }

    @Override
    public boolean canWander() {
        return !isSitting() && !isTame();
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        if (isTame()) {
            LivingEntity owner = getOwner();
            if (other == owner) return true;
            if (owner != null && other instanceof TameableKaijuEntity t && t.isTame() && owner.getUUID().equals(t.getOwnerUUID())) return true;
            if (owner != null) return owner.isAlliedTo(other);
        }
        return super.isAlliedTo(other);
    }

    @Override
    public boolean wantsToAttack(LivingEntity other) {
        // A tamed kaiju only picks fights with hostile kaiju by itself; otherwise it follows its owner's lead.
        if (isTame() && !(other instanceof KaijuEntity)) return false;
        return super.wantsToAttack(other);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && isSitting() && source.getEntity() != null) setSitting(false);
        return hurt;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 7 || id == 6) {
            ParticleOptions particle = id == 7 ? ParticleTypes.HEART : ParticleTypes.SMOKE;
            for (int i = 0; i < 12; i++) {
                level().addParticle(particle, getRandomX(1.0), getRandomY() + 0.5, getRandomZ(1.0),
                        random.nextGaussian() * 0.02, random.nextGaussian() * 0.02, random.nextGaussian() * 0.02);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (getOwnerUUID() != null) tag.putUUID("Owner", getOwnerUUID());
        tag.putBoolean("Sitting", isSitting());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_OWNER, tag.hasUUID("Owner") ? Optional.of(tag.getUUID("Owner")) : Optional.empty());
        setSitting(tag.getBoolean("Sitting"));
    }
}

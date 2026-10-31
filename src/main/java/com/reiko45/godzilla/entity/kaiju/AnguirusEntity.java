package com.reiko45.godzilla.entity.kaiju;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.VehicleWeapon;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.RoarAbility;
import com.reiko45.godzilla.entity.ability.RollAbility;
import com.reiko45.godzilla.entity.ai.AnguirusGoals;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Anguirus, Godzilla's most loyal ally. Its spiked shell hurts anyone who strikes it in melee and it rolls into
 * enemies as a spiked "Thunderball".
 * <p>
 * Tame it by feeding it meat. Once tamed: sneak + use to make it sit, use a saddle on it, then ride it. While riding,
 * press the Fire Weapon key (R) to roll in the direction you are looking.
 */
public class AnguirusEntity extends KaijuEntity implements OwnableEntity, VehicleWeapon {
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER = SynchedEntityData.defineId(AnguirusEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> DATA_SADDLED = SynchedEntityData.defineId(AnguirusEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SITTING = SynchedEntityData.defineId(AnguirusEntity.class, EntityDataSerializers.BOOLEAN);
    private static final RawAnimation SIT = RawAnimation.begin().thenLoop("sit");
    private static final int ROLL = 0;

    public AnguirusEntity(EntityType<? extends AnguirusEntity> type, Level level) {
        super(type, level);
        this.xpReward = 300;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(600, 22, 10, 22, 0.28).add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    @Override
    public KaijuFaction getFaction() {
        return KaijuFaction.TITAN;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new AnguirusGoals.Sit(this));
        this.goalSelector.addGoal(4, new AnguirusGoals.FollowOwner(this));
        this.targetSelector.addGoal(1, new AnguirusGoals.DefendOwner(this));
        this.targetSelector.addGoal(2, new AnguirusGoals.AssistOwner(this));
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new RollAbility());
        list.add(new RoarAbility(ModSounds.ANGUIRUS_ROAR));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 2.2f, 2.2f, new Vec3(0, 2.4, 3.9), 1.4f),
                new KaijuPart(this, "tail", 2.0f, 1.6f, new Vec3(0, 1.0, -4.8), 0.7f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 3.2, 4.6);
    }

    @Override
    protected double baseStepHeight() {
        return 2.5;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_OWNER, Optional.empty());
        builder.define(DATA_SADDLED, false);
        builder.define(DATA_SITTING, false);
    }

    // ------------------------------------------------------------------ taming

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return entityData.get(DATA_OWNER).orElse(null);
    }

    public boolean isTame() {
        return getOwnerUUID() != null;
    }

    public boolean isOwnedBy(LivingEntity entity) {
        return entity.getUUID().equals(getOwnerUUID());
    }

    public boolean isSaddled() {
        return entityData.get(DATA_SADDLED);
    }

    public boolean isSitting() {
        return entityData.get(DATA_SITTING);
    }

    public void setSitting(boolean sitting) {
        entityData.set(DATA_SITTING, sitting);
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
            if (owner != null && (other instanceof AnguirusEntity a && a.isTame() && owner.getUUID().equals(a.getOwnerUUID()))) return true;
            if (owner != null) return owner.isAlliedTo(other);
        }
        return super.isAlliedTo(other);
    }

    @Override
    public boolean wantsToAttack(LivingEntity other) {
        // Tamed Anguirus only fights invaders on its own; otherwise it follows its owner's lead.
        if (isTame() && !(other instanceof KaijuEntity)) return false;
        return super.wantsToAttack(other);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isTame()) {
            if (stack.is(ItemTags.MEAT)) {
                stack.consume(1, player);
                if (!level().isClientSide) {
                    if (random.nextInt(5) == 0) {
                        entityData.set(DATA_OWNER, Optional.of(player.getUUID()));
                        setTarget(null);
                        level().broadcastEntityEvent(this, (byte) 7);
                        player.displayClientMessage(Component.translatable("message.reikogodzilla.anguirus_tamed"), true);
                    } else {
                        level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
            return super.mobInteract(player, hand);
        }
        if (!isOwnedBy(player)) return super.mobInteract(player, hand);

        if (stack.is(ItemTags.MEAT) && getHealth() < getMaxHealth()) {
            stack.consume(1, player);
            heal(40);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (stack.is(Items.SADDLE) && !isSaddled()) {
            stack.consume(1, player);
            entityData.set(DATA_SADDLED, true);
            playSound(SoundEvents.HORSE_SADDLE, 1f, 0.8f);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (player.isSecondaryUseActive()) {
            setSitting(!isSitting());
            if (!level().isClientSide) {
                player.displayClientMessage(Component.translatable(isSitting() ? "message.reikogodzilla.anguirus_sit" : "message.reikogodzilla.anguirus_follow"), true);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (isSaddled() && !isVehicle()) {
            if (!level().isClientSide) {
                setSitting(false);
                player.startRiding(this);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
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

    // ------------------------------------------------------------------ spiked shell

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && source.getDirectEntity() instanceof LivingEntity attacker && attacker == source.getEntity()
                && attacker.distanceToSqr(this) < Math.pow(getBbWidth() + 4, 2)) {
            attacker.hurt(damageSources().thorns(this), 4.0f);
        }
        if (hurt && isSitting() && source.getEntity() != null) setSitting(false);
        return hurt;
    }

    // ------------------------------------------------------------------ riding

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (isSaddled() && getFirstPassenger() instanceof Player player) return player;
        return super.getControllingPassenger();
    }

    @Override
    protected void tickRidden(Player player, Vec3 input) {
        super.tickRidden(player, input);
        setRot(player.getYRot(), player.getXRot() * 0.5f);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 input) {
        if (isRolling()) return new Vec3(0, 0, 1);
        float strafe = player.xxa * 0.5f;
        float forward = player.zza;
        if (forward <= 0) forward *= 0.25f;
        return new Vec3(strafe, 0, forward);
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return (float) getAttributeValue(Attributes.MOVEMENT_SPEED) * (isRolling() ? 3.0f : 0.9f);
    }

    /** Synced, so the rider's client can speed up the roll it is simulating. */
    private boolean isRolling() {
        return getActiveAbility() instanceof RollAbility && getAbilityPhase() == PHASE_ACTIVE;
    }

    @Override
    public void fireWeapon(ServerPlayer rider) {
        if (isAbilityActive() || !abilities.get(ROLL).isReady(this)) return;
        RollAbility roll = (RollAbility) abilities.get(ROLL);
        startAbility(ROLL, null);
        roll.setDirection(rider.getLookAngle());
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (isSaddled()) spawnAtLocation(Items.SADDLE);
    }

    // ------------------------------------------------------------------ misc

    @Override
    protected PlayState mainAnimation(AnimationState<KaijuEntity> state) {
        if (isSitting() && !isAbilityActive()) return state.setAndContinue(SIT);
        return super.mainAnimation(state);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (getOwnerUUID() != null) tag.putUUID("Owner", getOwnerUUID());
        tag.putBoolean("Saddled", isSaddled());
        tag.putBoolean("Sitting", isSitting());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_OWNER, tag.hasUUID("Owner") ? Optional.of(tag.getUUID("Owner")) : Optional.empty());
        entityData.set(DATA_SADDLED, tag.getBoolean("Saddled"));
        setSitting(tag.getBoolean("Sitting"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ANGUIRUS_ROAR.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ANGUIRUS_ROAR.get();
    }
}

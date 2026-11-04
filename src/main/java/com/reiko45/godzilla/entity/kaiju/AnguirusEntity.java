package com.reiko45.godzilla.entity.kaiju;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.TameableKaijuEntity;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.RoarAbility;
import com.reiko45.godzilla.entity.ability.RollAbility;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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

import java.util.List;

/**
 * Anguirus, Godzilla's most loyal ally. Its spiked shell hurts anyone who strikes it in melee and it rolls into
 * enemies as a spiked "Thunderball".
 * <p>
 * Tame it by feeding it meat. Once tamed: sneak + use to make it sit, use a saddle on it, then ride it. While riding,
 * press the Fire key (R) to roll in the direction you are looking.
 */
public class AnguirusEntity extends TameableKaijuEntity {
    private static final EntityDataAccessor<Boolean> DATA_SADDLED = SynchedEntityData.defineId(AnguirusEntity.class, EntityDataSerializers.BOOLEAN);
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
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.TITAN;
    }

    /** Final Wars Anguirus was a Xilien weapon (M-Base); the Kilaaks controlled the Showa Anguirus. */
    @Override
    public boolean hasMBase() {
        return true;
    }

    @Override
    public boolean controllableBy(com.reiko45.godzilla.world.AlienSignal signal) {
        return signal == com.reiko45.godzilla.world.AlienSignal.KILAAK || super.controllableBy(signal);
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new RollAbility());
        list.add(new RoarAbility(ModSounds.ANGUIRUS_ROAR));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 2.2f, 2.2f, new Vec3(0, 1.7, 3.2), 1.4f),
                new KaijuPart(this, "tail", 2.0f, 1.6f, new Vec3(0, 1.0, -4.8), 0.7f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 2.1, 4.25);
    }

    @Override
    protected double baseStepHeight() {
        return 2.5;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SADDLED, false);
    }

    public boolean isSaddled() {
        return entityData.get(DATA_SADDLED);
    }

    // ------------------------------------------------------------------ taming & riding

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isTame()) {
            if (stack.is(ItemTags.MEAT)) {
                stack.consume(1, player);
                if (!level().isClientSide) {
                    if (random.nextInt(5) == 0) {
                        setOwner(player);
                        setTarget(null);
                        showTamingResult(true);
                        player.displayClientMessage(Component.translatable("message.reikogodzilla.anguirus_tamed"), true);
                    } else {
                        showTamingResult(false);
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
            toggleSitting(player);
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
    public boolean canBePilotedBy(Player player) {
        return isSaddled() && isOwnedBy(player);
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 input) {
        if (isRolling()) return new Vec3(0, 0, 1);
        return super.getRiddenInput(player, input);
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
    protected void onPilotAction(ServerPlayer pilot, int action) {
        if (action != ACTION_PRIMARY) return;
        if (tryPilotAbility(ROLL, pilot)) ((RollAbility) abilities.get(ROLL)).setDirection(pilot.getLookAngle());
    }

    // ------------------------------------------------------------------ spiked shell

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && source.getDirectEntity() instanceof LivingEntity attacker && attacker == source.getEntity()
                && attacker.distanceToSqr(this) < Math.pow(getBbWidth() + 4, 2)) {
            attacker.hurt(damageSources().thorns(this), 4.0f);
        }
        return hurt;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (isSaddled()) spawnAtLocation(Items.SADDLE);
    }

    @Override
    protected PlayState mainAnimation(AnimationState<KaijuEntity> state) {
        if (isSitting() && !isAbilityActive()) return state.setAndContinue(SIT);
        return super.mainAnimation(state);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Saddled", isSaddled());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_SADDLED, tag.getBoolean("Saddled"));
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

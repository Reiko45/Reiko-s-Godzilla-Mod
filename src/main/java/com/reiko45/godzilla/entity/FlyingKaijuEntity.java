package com.reiko45.godzilla.entity;

import com.reiko45.godzilla.entity.ai.FlyingGoals;
import com.reiko45.godzilla.entity.ai.KaijuFlyingMoveControl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;

/** A kaiju that can take off, circle its prey and land again (King Ghidorah, Mothra, Rodan). */
public abstract class FlyingKaijuEntity extends KaijuEntity {
    private static final EntityDataAccessor<Boolean> DATA_FLYING = SynchedEntityData.defineId(FlyingKaijuEntity.class, EntityDataSerializers.BOOLEAN);

    protected FlyingKaijuEntity(EntityType<? extends FlyingKaijuEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected MoveControl createMoveControl() {
        return new KaijuFlyingMoveControl(this);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new FlyingGoals.TakeOff(this));
        this.goalSelector.addGoal(2, new FlyingGoals.Combat(this));
        this.goalSelector.addGoal(3, new FlyingGoals.Land(this));
        this.goalSelector.addGoal(4, new FlyingGoals.Wander(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLYING, false);
    }

    public boolean isFlying() {
        return entityData.get(DATA_FLYING);
    }

    public void setFlying(boolean flying) {
        entityData.set(DATA_FLYING, flying);
        setNoGravity(flying);
    }

    /** Whether to take to the air as soon as a fight starts. */
    public boolean prefersFlightInCombat() {
        return true;
    }

    public boolean canFlyNow() {
        return true;
    }

    /** Aircraft (Garuda, the Super X series) never land. */
    public boolean alwaysFlies() {
        return false;
    }

    /** Speed multiplier when a pilot is flying it. */
    protected double pilotFlightSpeed() {
        return 1.0;
    }

    /** 1-in-N chance per tick to land when idle in the air. */
    public int landChance() {
        return 600;
    }

    public void playWingSound() {
        playSound(SoundEvents.ENDER_DRAGON_FLAP, 4.0f + getScale(), 0.6f);
    }

    @Override
    public void travel(Vec3 input) {
        if (isFlying() && isControlledByLocalInstance() && getControllingPassenger() instanceof net.minecraft.world.entity.player.Player pilot) {
            // Fly where the pilot looks: W/S along the view, A/D strafe, no keys = hover.
            Vec3 look = pilot.getLookAngle();
            double yaw = getYRot() * net.minecraft.util.Mth.DEG_TO_RAD;
            Vec3 left = new Vec3(Math.cos(yaw), 0, Math.sin(yaw));
            double speed = getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.FLYING_SPEED)
                    * Math.max(1.0, Math.sqrt(getScale())) * pilotFlightSpeed();
            Vec3 want = look.scale(input.z * speed).add(left.scale(input.x * speed));
            setDeltaMovement(getDeltaMovement().lerp(want, 0.2));
            move(MoverType.SELF, getDeltaMovement());
            calculateEntityAnimation(true);
            return;
        }
        if (isFlying() && isControlledByLocalInstance()) {
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.91));
            calculateEntityAnimation(true);
        } else {
            super.travel(input);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide) {
            if (alwaysFlies() && !isFlying() && !isEmpDisabled()) setFlying(true);
            net.minecraft.world.entity.player.Player pilot = getPilot();
            if (pilot != null && !alwaysFlies()) {
                // Look up and push forward to take off; look down onto the ground to land.
                if (!isFlying() && canFlyNow() && !isEmpDisabled() && pilot.getXRot() < -25 && pilot.zza > 0) {
                    setFlying(true);
                    setDeltaMovement(getDeltaMovement().add(0, 0.6, 0));
                } else if (isFlying() && (onGround() || !canFlyNow()) && pilot.getXRot() > 20) {
                    setFlying(false);
                }
            }
        }
        if (!level().isClientSide && isFlying()) {
            if (tickCount % 30 == 0 && random.nextInt(3) == 0) playWingSound();
            if (isInWater()) setDeltaMovement(getDeltaMovement().add(0, 0.15, 0));
        }
    }

    @Override
    protected RawAnimation movementAnimation(AnimationState<KaijuEntity> state) {
        return isFlying() ? FLY : super.movementAnimation(state);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Flying", isFlying());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setFlying(tag.getBoolean("Flying"));
    }
}

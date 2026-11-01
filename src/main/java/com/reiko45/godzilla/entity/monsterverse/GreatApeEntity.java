package com.reiko45.godzilla.entity.monsterverse;

import com.reiko45.godzilla.entity.KaijuAligned;
import com.reiko45.godzilla.entity.KaijuFaction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * One of the Great Apes of the Hollow Earth (Godzilla x Kong: The New Empire), scaled down so a tribe of them fits
 * in a fight. Under the Skar King they attack on sight; once he falls they are free and only defend themselves.
 */
public class GreatApeEntity extends PathfinderMob implements GeoEntity, KaijuAligned {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private boolean enslaved;

    public GreatApeEntity(EntityType<? extends GreatApeEntity> type, Level level) {
        super(type, level);
        this.xpReward = 12;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 70).add(Attributes.ARMOR, 6).add(Attributes.ATTACK_DAMAGE, 9)
                .add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.FOLLOW_RANGE, 40).add(Attributes.STEP_HEIGHT, 1.5)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.5f));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, GreatApeEntity.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, p -> enslaved));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false,
                e -> enslaved && (e instanceof KongEntity || e instanceof SukoEntity)));
    }

    public boolean isEnslaved() {
        return enslaved;
    }

    public void setEnslaved(boolean enslaved) {
        this.enslaved = enslaved;
        if (!enslaved && getTarget() instanceof Player) setTarget(null);
    }

    @Override
    public KaijuFaction getFaction() {
        return enslaved ? KaijuFaction.INVADER : KaijuFaction.TITAN;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return !enslaved && distance > 128 * 128 && !hasCustomName();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Enslaved", enslaved);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        enslaved = tag.getBoolean("Enslaved");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.RAVAGER_AMBIENT;
    }

    @Override
    public float getVoicePitch() {
        return 1.4f;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> state.setAndContinue(state.isMoving() ? WALK : IDLE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}

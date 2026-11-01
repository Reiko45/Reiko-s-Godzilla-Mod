package com.reiko45.godzilla.entity.millennium;

import com.reiko45.godzilla.entity.KaijuAligned;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

/**
 * Meganulon (Rodan, Godzilla vs. Megaguirus): the larva of a prehistoric giant dragonfly. They hatch from egg masses
 * in water, hunt people in the sewers, and after a few minutes climb up and molt into flying Meganula.
 */
public class MeganulonEntity extends Monster implements GeoEntity, KaijuAligned {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final int MOLT_TICKS = 4800;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    @Nullable
    private BlockPos nest;
    private int age;

    public MeganulonEntity(EntityType<? extends MeganulonEntity> type, Level level) {
        super(type, level);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30).add(Attributes.ARMOR, 6).add(Attributes.ATTACK_DAMAGE, 6)
                .add(Attributes.MOVEMENT_SPEED, 0.28).add(Attributes.FOLLOW_RANGE, 32).add(Attributes.STEP_HEIGHT, 1.1);
    }

    public void setNest(@Nullable BlockPos nest) {
        this.nest = nest;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
    }

    @Override
    public KaijuFaction getFaction() {
        return KaijuFaction.INVADER;
    }

    /** Aquatic larvae: they don't drown. */
    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(net.minecraft.world.damagesource.DamageTypes.DROWN) || super.isInvulnerableTo(source);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel server) || !isAlive()) return;
        if (++age >= MOLT_TICKS) molt(server);
    }

    /** Climbs out of its shell as a Meganula. */
    private void molt(ServerLevel server) {
        MeganulaEntity adult = ModEntities.MEGANULA.get().create(server);
        if (adult == null) return;
        adult.moveTo(getX(), getY() + 0.5, getZ(), getYRot(), 0);
        adult.finalizeSpawn(server, server.getCurrentDifficultyAt(blockPosition()), MobSpawnType.CONVERSION, null);
        adult.setNest(nest);
        server.addFreshEntity(adult);
        server.sendParticles(ParticleTypes.ITEM_SLIME, getX(), getY() + 0.5, getZ(), 20, 0.5, 0.5, 0.5, 0.1);
        playSound(SoundEvents.TURTLE_EGG_HATCH, 1f, 0.6f);
        discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Age", age);
        if (nest != null) tag.put("Nest", NbtUtils.writeBlockPos(nest));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        age = tag.getInt("Age");
        nest = NbtUtils.readBlockPos(tag, "Nest").orElse(null);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.MEGANULON_CHITTER.get();
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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> state.setAndContinue(state.isMoving() ? WALK : IDLE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}

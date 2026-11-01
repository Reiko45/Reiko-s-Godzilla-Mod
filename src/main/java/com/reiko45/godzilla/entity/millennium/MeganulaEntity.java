package com.reiko45.godzilla.entity.millennium;

import com.reiko45.godzilla.block.MeganulonEggBlock;
import com.reiko45.godzilla.entity.KaijuAligned;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Meganula (Godzilla vs. Megaguirus): giant dragonflies that swarm over kaiju and drain their energy with their tail
 * stingers. Once full, a Meganula flies back to the egg mass it came from and feeds it; feed an egg mass enough and
 * Megaguirus, their queen, emerges.
 */
public class MeganulaEntity extends Monster implements GeoEntity, KaijuAligned {
    private static final RawAnimation FLY = RawAnimation.begin().thenLoop("fly");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    @Nullable
    private BlockPos nest;
    private float energy;

    public MeganulaEntity(EntityType<? extends MeganulaEntity> type, Level level) {
        super(type, level);
        this.xpReward = 6;
        this.moveControl = new FlyControl(this);
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 16).add(Attributes.ATTACK_DAMAGE, 3)
                .add(Attributes.MOVEMENT_SPEED, 0.6).add(Attributes.FLYING_SPEED, 0.6).add(Attributes.FOLLOW_RANGE, 40);
    }

    public void setNest(@Nullable BlockPos nest) {
        this.nest = nest;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ReturnToNestGoal());
        this.goalSelector.addGoal(2, new StingGoal());
        this.goalSelector.addGoal(5, new WanderGoal());
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, KaijuEntity.class, 10, true, false,
                e -> e instanceof KaijuEntity k && k.getFaction() != KaijuFaction.INVADER));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public KaijuFaction getFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, BlockPos pos) {}

    /** The stinger: drains a kaiju's energy, or a player's strength and experience. */
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (!hit || !(level() instanceof ServerLevel server)) return hit;
        if (target instanceof KaijuEntity kaiju) {
            KaijuDestruction.hurtIgnoringCooldown(kaiju, ModDamageTypes.source(server, ModDamageTypes.ENERGY_DRAIN, this), 4f);
            energy += kaiju.isGodzilla() ? 25f : 15f;
        } else if (target instanceof Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 1));
            player.giveExperiencePoints(-3);
            energy += 5f;
        }
        return true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Energy", energy);
        if (nest != null) tag.put("Nest", NbtUtils.writeBlockPos(nest));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        energy = tag.getFloat("Energy");
        nest = NbtUtils.readBlockPos(tag, "Nest").orElse(null);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.MEGAGUIRUS_BUZZ.get();
    }

    @Override
    public float getVoicePitch() {
        return 1.8f;
    }

    @Override
    protected float getSoundVolume() {
        return 0.6f;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BEE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BEE_DEATH;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> state.setAndContinue(FLY)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    // ---------------------------------------------------------------- flight

    private static class FlyControl extends MoveControl {
        FlyControl(Mob mob) {
            super(mob);
        }

        @Override
        public void tick() {
            if (operation != Operation.MOVE_TO) return;
            Vec3 to = new Vec3(wantedX - mob.getX(), wantedY - mob.getY(), wantedZ - mob.getZ());
            double d = to.length();
            if (d < mob.getBoundingBox().getSize()) {
                operation = Operation.WAIT;
                mob.setDeltaMovement(mob.getDeltaMovement().scale(0.5));
                return;
            }
            mob.setDeltaMovement(mob.getDeltaMovement().add(to.scale(speedModifier * 0.06 / d)));
            Vec3 v = mob.getDeltaMovement();
            if (mob.getTarget() == null) {
                mob.setYRot(-((float) Mth.atan2(v.x, v.z)) * Mth.RAD_TO_DEG);
            } else {
                LivingEntity t = mob.getTarget();
                mob.setYRot(-((float) Mth.atan2(t.getX() - mob.getX(), t.getZ() - mob.getZ())) * Mth.RAD_TO_DEG);
            }
            mob.yBodyRot = mob.getYRot();
        }
    }

    private class StingGoal extends Goal {
        StingGoal() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return getTarget() != null && getTarget().isAlive() && energy < 100f;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = getTarget();
            if (target == null) return;
            Vec3 aim = target.getBoundingBox().getCenter().add(0, target.getBbHeight() * 0.25, 0);
            moveControl.setWantedPosition(aim.x, aim.y, aim.z, 1.2);
            double reach = target.getBbWidth() * 0.6 + 1.6;
            if (getBoundingBox().inflate(reach).intersects(target.getBoundingBox()) && random.nextInt(8) == 0) doHurtTarget(target);
        }
    }

    private class ReturnToNestGoal extends Goal {
        ReturnToNestGoal() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return energy >= 100f && nest != null;
        }

        @Override
        public void tick() {
            if (nest == null) return;
            moveControl.setWantedPosition(nest.getX() + 0.5, nest.getY() + 1.5, nest.getZ() + 0.5, 1.2);
            if (distanceToSqr(nest.getX() + 0.5, nest.getY() + 1, nest.getZ() + 0.5) < 9) {
                if (level() instanceof ServerLevel server && !MeganulonEggBlock.feed(server, nest)) nest = null;
                energy = 0;
            }
        }
    }

    private class WanderGoal extends Goal {
        WanderGoal() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !moveControl.hasWanted() && random.nextInt(7) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            BlockPos base = nest != null && random.nextBoolean() ? nest : blockPosition();
            for (int i = 0; i < 3; i++) {
                BlockPos p = base.offset(random.nextInt(15) - 7, random.nextInt(9) - 3, random.nextInt(15) - 7);
                if (level().isEmptyBlock(p)) {
                    moveControl.setWantedPosition(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 0.6);
                    break;
                }
            }
        }
    }
}

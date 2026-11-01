package com.reiko45.godzilla.entity.monsterverse;

import com.reiko45.godzilla.entity.KaijuAligned;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Skullcrawlers (Kong: Skull Island): two-armed reptiles that live in the caverns under Skull Island and eat
 * anything. They hunt in packs, and their tongues drag prey into their jaws.
 */
public class SkullcrawlerEntity extends Monster implements GeoEntity, KaijuAligned {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public SkullcrawlerEntity(EntityType<? extends SkullcrawlerEntity> type, Level level) {
        super(type, level);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 40).add(Attributes.ARMOR, 4).add(Attributes.ATTACK_DAMAGE, 7)
                .add(Attributes.MOVEMENT_SPEED, 0.32).add(Attributes.FOLLOW_RANGE, 40).add(Attributes.STEP_HEIGHT, 1.1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.45f));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.25, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, SkullcrawlerEntity.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Animal.class, 10, true, false, e -> e.getBbWidth() > 0.8f));
    }

    @Override
    public KaijuFaction getFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living && random.nextInt(4) == 0) {
            // The tongue lashes out and reels the victim in.
            Vec3 pull = position().subtract(living.position()).normalize().scale(0.8);
            living.push(pull.x, 0.2, pull.z);
            living.hurtMarked = true;
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2));
        }
        return hit;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.SKULLCRAWLER_HISS.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.HOGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.HOGLIN_DEATH;
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

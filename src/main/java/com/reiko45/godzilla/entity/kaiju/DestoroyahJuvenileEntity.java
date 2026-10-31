package com.reiko45.godzilla.entity.kaiju;

import com.reiko45.godzilla.entity.KaijuAligned;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

/**
 * Destoroyah juvenile: a person-sized crustacean that hunts in packs. When enough of them gather for long enough, they
 * merge into a Destoroyah aggregate.
 */
public class DestoroyahJuvenileEntity extends Monster implements GeoEntity, KaijuAligned {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final int MERGE_COUNT = 5;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public DestoroyahJuvenileEntity(EntityType<? extends DestoroyahJuvenileEntity> type, Level level) {
        super(type, level);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 40).add(Attributes.ARMOR, 6)
                .add(Attributes.ATTACK_DAMAGE, 7).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.FOLLOW_RANGE, 32);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, DestoroyahJuvenileEntity.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
    }

    @Override
    public KaijuFaction getFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) living.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
        return hit;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server) || tickCount < 1200 || tickCount % 100 != 0) return;
        List<DestoroyahJuvenileEntity> swarm = server.getEntitiesOfClass(DestoroyahJuvenileEntity.class, getBoundingBox().inflate(16, 24, 16), LivingEntity::isAlive);
        if (swarm.size() < MERGE_COUNT || swarm.stream().anyMatch(j -> j.getId() < getId())) return;
        // This one leads: the swarm fuses into an aggregate.
        DestoroyahEntity aggregate = ModEntities.DESTOROYAH.get().create(server);
        if (aggregate == null) return;
        aggregate.moveTo(getX(), getY(), getZ(), getYRot(), 0);
        aggregate.finalizeSpawn(server, server.getCurrentDifficultyAt(blockPosition()), MobSpawnType.CONVERSION, null);
        aggregate.setVariant(DestoroyahEntity.AGGREGATE);
        aggregate.setHealth(aggregate.getMaxHealth());
        server.addFreshEntity(aggregate);
        for (int i = 0; i < MERGE_COUNT; i++) {
            DestoroyahJuvenileEntity j = swarm.get(i);
            server.sendParticles(ParticleTypes.WITCH, j.getX(), j.getY(0.5), j.getZ(), 20, 0.5, 0.5, 0.5, 0.1);
            j.discard();
        }
        for (var p : server.players()) {
            if (p.distanceToSqr(aggregate) < 128 * 128) p.sendSystemMessage(Component.translatable("message.reikogodzilla.destoroyah_aggregate").withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SILVERFISH_AMBIENT;
    }

    @Override
    public float getVoicePitch() {
        return 0.6f;
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

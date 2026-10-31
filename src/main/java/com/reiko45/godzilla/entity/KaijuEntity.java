package com.reiko45.godzilla.entity;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.ability.BeamAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ai.*;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for every giant monster: scaling, boss bars, multipart hitboxes, special abilities (beams and so on),
 * faction-based targeting, block crushing and regeneration.
 */
public abstract class KaijuEntity extends PathfinderMob implements GeoEntity, BeamEmitter, KaijuAligned {
    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(KaijuEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ABILITY = SynchedEntityData.defineId(KaijuEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ABILITY_PHASE = SynchedEntityData.defineId(KaijuEntity.class, EntityDataSerializers.INT);
    @SuppressWarnings("unchecked")
    private static final EntityDataAccessor<Vector3f>[] DATA_BEAM_END = new EntityDataAccessor[]{
            SynchedEntityData.defineId(KaijuEntity.class, EntityDataSerializers.VECTOR3),
            SynchedEntityData.defineId(KaijuEntity.class, EntityDataSerializers.VECTOR3),
            SynchedEntityData.defineId(KaijuEntity.class, EntityDataSerializers.VECTOR3)
    };

    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    protected static final RawAnimation FLY = RawAnimation.begin().thenLoop("fly");
    protected static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    protected static final RawAnimation ROAR = RawAnimation.begin().thenPlay("roar");

    public static final int PHASE_CHARGE = 0;
    public static final int PHASE_ACTIVE = 1;

    protected final List<KaijuAbility> abilities = new ArrayList<>();
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent;
    private KaijuPart[] parts;

    private int abilityTick;
    @Nullable
    private LivingEntity abilityTarget;
    private boolean abilityEndRequested;
    public int globalAbilityCooldown = 60;
    public int meleeCooldown;
    protected int regenSuppressedTicks;
    @Nullable
    private BlockPos destination;
    private float lastPartScale = -1;

    protected KaijuEntity(EntityType<? extends KaijuEntity> type, Level level) {
        super(type, level);
        this.xpReward = 300;
        this.setPersistenceRequired();
        this.moveControl = createMoveControl();
        this.bossEvent = new ServerBossEvent(this.getDisplayName(), getFaction().barColor, BossEvent.BossBarOverlay.NOTCHED_10);
        registerAbilities(abilities);
        this.parts = createParts();
        if (parts.length > 0) {
            this.setId(ENTITY_COUNTER.getAndAdd(parts.length + 1) + 1);
        }
        applyScale();
    }

    // ---------------------------------------------------------------- setup hooks for subclasses

    /** Add this kaiju's special attacks. Called from the constructor, so don't rely on subclass fields. */
    protected abstract void registerAbilities(List<KaijuAbility> list);

    /** Extra hitboxes. Called from the constructor. */
    protected KaijuPart[] createParts() {
        return new KaijuPart[0];
    }

    protected MoveControl createMoveControl() {
        return new KaijuMoveControl(this);
    }

    /** Size multiplier for the current variant, on top of the global config scale. */
    protected double variantScale() {
        return 1.0;
    }

    /** Step height at scale 1. */
    protected double baseStepHeight() {
        return 2.0;
    }

    /** Where beam {@code index} starts, relative to the feet at scale 1 (x = left, y = up, z = forward). */
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, getBbHeight() / Math.max(0.01, getScale()) * 0.9, getBbWidth() / Math.max(0.01, getScale()) * 0.6);
    }

    /** Health regenerated per second (0 for none). */
    protected float regenPerSecond() {
        return 0.25f;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new KaijuAbilityGoal(this));
        this.goalSelector.addGoal(2, new KaijuMeleeGoal(this));
        this.goalSelector.addGoal(5, new KaijuWanderGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 48.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new KaijuTargetGoal(this));
    }

    /** Standard kaiju attributes; subclasses add or override values. */
    public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createKaijuAttributes(
            double health, double armor, double toughness, double attack, double speed) {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.ARMOR, armor)
                .add(Attributes.ARMOR_TOUGHNESS, toughness)
                .add(Attributes.ATTACK_DAMAGE, attack)
                .add(Attributes.ATTACK_KNOCKBACK, 2.0)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.FOLLOW_RANGE, 96.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.STEP_HEIGHT, 2.0)
                .add(Attributes.FLYING_SPEED, 0.6);
    }

    // ---------------------------------------------------------------- synced data

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, 0);
        builder.define(DATA_ABILITY, -1);
        builder.define(DATA_ABILITY_PHASE, 0);
        for (EntityDataAccessor<Vector3f> a : DATA_BEAM_END) builder.define(a, new Vector3f());
    }

    public int getVariant() {
        return entityData.get(DATA_VARIANT);
    }

    public void setVariant(int variant) {
        entityData.set(DATA_VARIANT, variant);
        applyScale();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_VARIANT.equals(key)) refreshDimensions();
    }

    // ---------------------------------------------------------------- scale

    public void applyScale() {
        double scale = Mth.clamp(variantScale() * GodzillaConfig.get(GodzillaConfig.KAIJU_SCALE, 1.0), 0.0625, 16.0);
        AttributeInstance s = getAttribute(Attributes.SCALE);
        if (s != null && s.getBaseValue() != scale) s.setBaseValue(scale);
        AttributeInstance step = getAttribute(Attributes.STEP_HEIGHT);
        if (step != null) step.setBaseValue(Math.min(10.0, baseStepHeight() * scale));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, data);
        applyScale();
        setHealth(getMaxHealth());
        return result;
    }

    // ---------------------------------------------------------------- multipart

    @Override
    public boolean isMultipartEntity() {
        return parts.length > 0;
    }

    @Nullable
    @Override
    public PartEntity<?>[] getParts() {
        return parts;
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        if (parts != null) {
            for (int i = 0; i < parts.length; i++) parts[i].setId(id + i + 1);
        }
    }

    public boolean hurtFromPart(KaijuPart part, DamageSource source, float amount) {
        return this.hurt(source, amount * part.damageMultiplier);
    }

    protected void updateParts() {
        float scale = getScale();
        if (scale != lastPartScale) {
            lastPartScale = scale;
            for (KaijuPart p : parts) p.refreshDimensions();
        }
        float yawRad = this.yBodyRot * Mth.DEG_TO_RAD;
        for (KaijuPart p : parts) {
            Vec3 off = p.offset.scale(scale).yRot(-yawRad);
            p.xo = p.getX();
            p.yo = p.getY();
            p.zo = p.getZ();
            p.xOld = p.getX();
            p.yOld = p.getY();
            p.zOld = p.getZ();
            p.setPos(getX() + off.x, getY() + off.y, getZ() + off.z);
        }
    }

    // ---------------------------------------------------------------- abilities

    public List<KaijuAbility> getAbilities() {
        return abilities;
    }

    public boolean isAbilityActive() {
        return entityData.get(DATA_ABILITY) >= 0;
    }

    @Nullable
    public KaijuAbility getActiveAbility() {
        int i = entityData.get(DATA_ABILITY);
        return i >= 0 && i < abilities.size() ? abilities.get(i) : null;
    }

    public int getAbilityPhase() {
        return entityData.get(DATA_ABILITY_PHASE);
    }

    public int getAbilityTick() {
        return abilityTick;
    }

    public void startAbility(int index, @Nullable LivingEntity target) {
        KaijuAbility ability = abilities.get(index);
        entityData.set(DATA_ABILITY, index);
        entityData.set(DATA_ABILITY_PHASE, ability.chargeTicks > 0 ? PHASE_CHARGE : PHASE_ACTIVE);
        abilityTick = 0;
        abilityTarget = target;
        abilityEndRequested = false;
        setZza(0);
        getNavigation().stop();
        ability.start(this, target);
        if (ability.chargeTicks <= 0) ability.activate(this, target);
    }

    /** Lets an ability finish early. */
    public void requestAbilityEnd() {
        abilityEndRequested = true;
    }

    public void stopAbility() {
        KaijuAbility ability = getActiveAbility();
        if (ability != null) {
            ability.stop(this);
            ability.resetCooldown();
        }
        entityData.set(DATA_ABILITY, -1);
        abilityTarget = null;
        globalAbilityCooldown = 30 + random.nextInt(30);
    }

    private void tickAbility() {
        KaijuAbility ability = getActiveAbility();
        if (ability == null) return;
        LivingEntity target = abilityTarget != null && abilityTarget.isAlive() ? abilityTarget : getTarget();
        if (getAbilityPhase() == PHASE_CHARGE) {
            ability.tickCharge(this, target, abilityTick);
            if (++abilityTick >= ability.chargeTicks) {
                abilityTick = 0;
                entityData.set(DATA_ABILITY_PHASE, PHASE_ACTIVE);
                ability.activate(this, target);
            }
        } else {
            ability.tickActive(this, target, abilityTick);
            if (++abilityTick >= ability.activeTicks || abilityEndRequested) stopAbility();
        }
    }

    // ---------------------------------------------------------------- beams (BeamEmitter)

    public void setBeamEnd(int index, Vec3 end) {
        entityData.set(DATA_BEAM_END[index], new Vector3f((float) end.x, (float) end.y, (float) end.z));
    }

    @Override
    public int getActiveBeamCount() {
        if (getAbilityPhase() != PHASE_ACTIVE) return 0;
        return getActiveAbility() instanceof BeamAbility beam ? beam.emitters : 0;
    }

    @Override
    public Vec3 getBeamOrigin(int index, float partialTick) {
        float yaw = Mth.rotLerp(partialTick, yBodyRotO, yBodyRot) * Mth.DEG_TO_RAD;
        Vec3 local = beamLocalOffset(index).scale(getScale()).yRot(-yaw);
        return getPosition(partialTick).add(local);
    }

    @Override
    public Vec3 getBeamEnd(int index) {
        Vector3f v = entityData.get(DATA_BEAM_END[Mth.clamp(index, 0, 2)]);
        return new Vec3(v.x, v.y, v.z);
    }

    @Override
    public BeamStyle getBeamStyle() {
        return getActiveAbility() instanceof BeamAbility beam ? beam.style : BeamStyle.ATOMIC;
    }

    @Override
    public float getBeamWidth() {
        return getActiveAbility() instanceof BeamAbility beam ? beam.width * getScale() : 0.5f;
    }

    // ---------------------------------------------------------------- targeting

    @Override
    public abstract KaijuFaction getFaction();

    /** Whether this kaiju picks a fight with {@code other} unprovoked. */
    public boolean wantsToAttack(LivingEntity other) {
        if (other == this || !other.isAlive() || isAlliedTo(other)) return false;
        if (other instanceof KaijuAligned aligned) return getFaction().isHostileTo(aligned.getFaction());
        if (other instanceof Player player) {
            return !player.isCreative() && !player.isSpectator() && attacksPlayersOnSight();
        }
        return false;
    }

    protected boolean attacksPlayersOnSight() {
        return getFaction() == KaijuFaction.INVADER;
    }

    @Nullable
    public BlockPos getDestination() {
        return destination;
    }

    /** A place the kaiju heads for when it has nothing to fight (used by events: "Godzilla is coming ashore"). */
    public void setDestination(@Nullable BlockPos destination) {
        this.destination = destination;
    }

    /** False while sitting, cocooned and so on. */
    public boolean canWander() {
        return true;
    }

    public double getMeleeReach(LivingEntity target) {
        return (getBbWidth() + target.getBbWidth()) / 2.0 + 2.0 * getScale();
    }

    public void performMelee(LivingEntity target) {
        meleeCooldown = 30;
        swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        triggerAnim("action", "attack");
        doHurtTarget(target);
    }

    // ---------------------------------------------------------------- tick

    @Override
    public void aiStep() {
        super.aiStep();
        if (parts.length > 0) updateParts();
        if (!level().isClientSide && isAlive()) {
            Vec3 motion = getDeltaMovement();
            boolean moving = motion.horizontalDistanceSqr() > 0.0016;
            if (horizontalCollision && tickCount % 2 == 0 && KaijuDestruction.canGrief(level(), this)) {
                crushBlocksAhead(motion);
            }
            if (moving && tickCount % 10 == 0) stompSmallEntities();
        }
    }

    protected void crushBlocksAhead(Vec3 motion) {
        Vec3 dir = motion.horizontalDistanceSqr() > 1.0E-4 ? motion.multiply(1, 0, 1).normalize() : Vec3.directionFromRotation(0, getYRot());
        AABB box = getBoundingBox().move(dir.scale(0.8));
        box = new AABB(box.minX, getY() + 1.0, box.minZ, box.maxX, box.maxY, box.maxZ);
        int budget = GodzillaConfig.get(GodzillaConfig.MAX_BLOCKS_PER_TICK, 48);
        KaijuDestruction.breakBox((ServerLevel) level(), box, budget);
    }

    protected void stompSmallEntities() {
        AABB feet = getBoundingBox();
        feet = new AABB(feet.minX - 0.5, feet.minY - 0.5, feet.minZ - 0.5, feet.maxX + 0.5, feet.minY + Math.max(1.5, getBbHeight() * 0.2), feet.maxZ + 0.5);
        DamageSource stomp = ModDamageTypes.source(level(), ModDamageTypes.KAIJU_STOMP, this);
        for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, feet)) {
            if (e == this || e instanceof KaijuEntity || e.isPassengerOfSameVehicle(this) || hasPassenger(e) || isAlliedTo(e)) continue;
            if (e.getBbHeight() > getBbHeight() * 0.4f) continue;
            if (e instanceof Player p && (p.isCreative() || p.isSpectator())) continue;
            e.hurt(stomp, 6.0f * getScale());
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        for (KaijuAbility a : abilities) a.tickCooldown();
        if (globalAbilityCooldown > 0) globalAbilityCooldown--;
        if (meleeCooldown > 0) meleeCooldown--;
        if (regenSuppressedTicks > 0) regenSuppressedTicks--;
        tickAbility();

        if (tickCount % 20 == 0) {
            float regen = regenPerSecond();
            if (regen > 0 && regenSuppressedTicks <= 0 && getHealth() < getMaxHealth()) heal(regen);
            updateBossBarViewers();
        }
        bossEvent.setProgress(getHealth() / getMaxHealth());
    }

    /** Blocks natural regeneration (Oxygen Destroyer, later cadmium) for a while. */
    public void suppressRegeneration(int ticks) {
        regenSuppressedTicks = Math.max(regenSuppressedTicks, ticks);
    }

    // ---------------------------------------------------------------- boss bar

    private void updateBossBarViewers() {
        if (!(level() instanceof ServerLevel server)) return;
        bossEvent.setName(getDisplayName());
        boolean enabled = GodzillaConfig.get(GodzillaConfig.BOSS_BARS, true);
        for (ServerPlayer p : List.copyOf(bossEvent.getPlayers())) {
            if (!enabled || !p.isAlive() || p.level() != level() || p.distanceToSqr(this) > 112 * 112) bossEvent.removePlayer(p);
        }
        if (!enabled) return;
        for (ServerPlayer p : server.players()) {
            if (p.distanceToSqr(this) < 96 * 96) bossEvent.addPlayer(p);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        bossEvent.removeAllPlayers();
    }

    @Override
    public void setCustomName(@Nullable net.minecraft.network.chat.Component name) {
        super.setCustomName(name);
        bossEvent.setName(getDisplayName());
    }

    // ---------------------------------------------------------------- damage & misc behaviour

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CRAMMING) || source.is(DamageTypes.DROWN)
                || source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypes.FLY_INTO_WALL) || source.is(DamageTypes.SWEET_BERRY_BUSH)
                || source.is(DamageTypes.CACTUS)) {
            return true;
        }
        return super.isInvulnerableTo(source);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    /** Kaiju wade along the sea floor at full speed instead of swimming slowly. */
    @Override
    public boolean isAffectedByFluids() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public int getMaxHeadYRot() {
        return 40;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 500;
    }

    @Override
    protected float getSoundVolume() {
        return 4.0f + getScale();
    }

    @Override
    public float getVoicePitch() {
        return (random.nextFloat() - random.nextFloat()) * 0.1f + 1.0f;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        AABB box = getBoundingBox().inflate(getBbWidth() * 2.5, getBbHeight() * 0.5, getBbWidth() * 2.5);
        int beams = getActiveBeamCount();
        for (int i = 0; i < beams; i++) box = box.minmax(new AABB(getBeamEnd(i), getBeamEnd(i)).inflate(2));
        return box;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", getVariant());
        if (destination != null) tag.put("Destination", NbtUtils.writeBlockPos(destination));
        tag.putInt("RegenSuppressed", regenSuppressedTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_VARIANT, tag.getInt("Variant"));
        destination = NbtUtils.readBlockPos(tag, "Destination").orElse(null);
        regenSuppressedTicks = tag.getInt("RegenSuppressed");
        applyScale();
        if (hasCustomName()) bossEvent.setName(getDisplayName());
    }

    // ---------------------------------------------------------------- GeckoLib

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, this::mainAnimation));
        controllers.add(new AnimationController<>(this, "action", 2, state -> PlayState.STOP)
                .triggerableAnim("attack", ATTACK)
                .triggerableAnim("roar", ROAR));
    }

    protected PlayState mainAnimation(AnimationState<KaijuEntity> state) {
        KaijuAbility ability = getActiveAbility();
        if (ability != null) {
            return state.setAndContinue(getAbilityPhase() == PHASE_CHARGE ? ability.chargeAnimation : ability.activeAnimation);
        }
        return state.setAndContinue(movementAnimation(state));
    }

    protected RawAnimation movementAnimation(AnimationState<KaijuEntity> state) {
        return state.isMoving() ? WALK : IDLE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}

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
public abstract class KaijuEntity extends PathfinderMob implements GeoEntity, BeamEmitter, KaijuAligned, VehicleWeapon {
    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(KaijuEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ABILITY = SynchedEntityData.defineId(KaijuEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ABILITY_PHASE = SynchedEntityData.defineId(KaijuEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_FREEZE = SynchedEntityData.defineId(KaijuEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_FROZEN = SynchedEntityData.defineId(KaijuEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_CADMIUM = SynchedEntityData.defineId(KaijuEntity.class, EntityDataSerializers.BOOLEAN);
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
    private int frozenTicks;
    private int cadmiumTicks;

    /** Pilot actions sent by the rider's keys. */
    public static final int ACTION_PRIMARY = 0;
    public static final int ACTION_SECONDARY = 1;
    public static final int ACTION_TRANSFORM = 2;

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
        builder.define(DATA_FREEZE, 0f);
        builder.define(DATA_FROZEN, false);
        builder.define(DATA_CADMIUM, false);
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
        if (isFrozenSolid()) return;
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
        if (other instanceof KaijuAligned aligned) {
            if (getFaction().isHostileTo(aligned.getFaction())) return true;
            // Human tech also engages any kaiju that is attacking people.
            return getFaction() == KaijuFaction.HUMAN && other instanceof Mob mob
                    && (mob.getTarget() instanceof Player || mob.getTarget() instanceof KaijuAligned t && t.getFaction() == KaijuFaction.HUMAN);
        }
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

    /** Whether walking into blocks smashes them (tamed kaiju leave your base alone). */
    public boolean canCrushBlocks() {
        return true;
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
        if (!level().isClientSide && isAlive()) tickWeaknesses();
        if (level().isClientSide && isFrozenSolid() && random.nextInt(3) == 0) {
            level().addParticle(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE, getRandomX(0.8), getRandomY(), getRandomZ(0.8), 0, -0.02, 0);
        }
        if (!level().isClientSide && isAlive()) {
            Vec3 motion = getDeltaMovement();
            boolean moving = motion.horizontalDistanceSqr() > 0.0016;
            if (horizontalCollision && tickCount % 2 == 0 && canCrushBlocks() && KaijuDestruction.canGrief(level(), this)) {
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
            if (e instanceof Player p && (p.isCreative() || p.isSpectator() || getFaction() == KaijuFaction.HUMAN)) continue;
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
            if (regen > 0 && regenSuppressedTicks <= 0 && !isCadmiumSuppressed() && getHealth() < getMaxHealth()) heal(regen);
            updateBossBarViewers();
        }
        bossEvent.setProgress(getHealth() / getMaxHealth());
    }

    /** Blocks natural regeneration (Oxygen Destroyer, later cadmium) for a while. */
    public void suppressRegeneration(int ticks) {
        regenSuppressedTicks = Math.max(regenSuppressedTicks, ticks);
    }

    // ---------------------------------------------------------------- boss bar

    /** Re-colours the boss bar after a change of sides. */
    protected void refreshBossColor() {
        if (bossEvent.getColor() != getFaction().barColor) bossEvent.setColor(getFaction().barColor);
    }

    private void updateBossBarViewers() {
        if (!(level() instanceof ServerLevel server)) return;
        bossEvent.setName(getDisplayName());
        boolean enabled = GodzillaConfig.get(GodzillaConfig.BOSS_BARS, true) && showsBossBar();
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

    // ---------------------------------------------------------------- weaknesses: freeze, cadmium (Phase 2)

    public float getFreeze() {
        return entityData.get(DATA_FREEZE);
    }

    public boolean isFrozenSolid() {
        return entityData.get(DATA_FROZEN);
    }

    public boolean isCadmiumSuppressed() {
        return entityData.get(DATA_CADMIUM);
    }

    /** How quickly freezer weapons work on this kaiju (Destoroyah is very vulnerable). */
    public float freezeMultiplier() {
        return 1.0f;
    }

    /**
     * Freezer weapons (Super X3, DAG-MB96 tanks). At 100 the kaiju freezes solid for a few seconds: it can't move or
     * attack and takes extra damage.
     */
    public void addFreeze(float amount) {
        if (isFrozenSolid()) {
            frozenTicks = Math.min(frozenTicks + (int) amount, 200);
            return;
        }
        float value = Math.min(100f, getFreeze() + amount * freezeMultiplier());
        entityData.set(DATA_FREEZE, value);
        if (value >= 100f) {
            frozenTicks = (int) GodzillaConfig.get(GodzillaConfig.FROZEN_TICKS, 120);
            entityData.set(DATA_FROZEN, true);
            if (isAbilityActive()) stopAbility();
            playSound(net.minecraft.sounds.SoundEvents.GLASS_BREAK, 4f, 0.5f);
        }
    }

    /**
     * Cadmium (Super X, Super X3) dampens nuclear reactions: no regeneration and no nuclear attacks for a while.
     */
    public void applyCadmium(int ticks) {
        cadmiumTicks = Math.max(cadmiumTicks, ticks);
        entityData.set(DATA_CADMIUM, true);
        if (getActiveAbility() instanceof BeamAbility beam && beam.isNuclear()) stopAbility();
    }

    private void tickWeaknesses() {
        if (frozenTicks > 0) {
            if (--frozenTicks <= 0) {
                entityData.set(DATA_FROZEN, false);
                entityData.set(DATA_FREEZE, 40f);
                playSound(net.minecraft.sounds.SoundEvents.GLASS_BREAK, 4f, 0.8f);
            } else if (tickCount % 4 == 0 && level() instanceof ServerLevel server) {
                server.sendParticles(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE, getX(), getY(0.5), getZ(), 8,
                        getBbWidth() * 0.5, getBbHeight() * 0.4, getBbWidth() * 0.5, 0.01);
            }
        } else if (getFreeze() > 0) {
            float thaw = isOnFire() || isInLava() ? 2.0f : 0.25f;
            entityData.set(DATA_FREEZE, Math.max(0f, getFreeze() - thaw));
        }
        if (cadmiumTicks > 0 && --cadmiumTicks <= 0) entityData.set(DATA_CADMIUM, false);
    }

    @Override
    public boolean isImmobile() {
        return isFrozenSolid() || super.isImmobile();
    }

    /**
     * Colour multiplier for the renderer: icy blue while freezing, or null for none. Subclasses add their own
     * (Burning Godzilla glows orange).
     */
    @Nullable
    public float[] getTint() {
        float f = isFrozenSolid() ? 1.0f : getFreeze() / 100f * 0.6f;
        if (f <= 0.01f) return null;
        return new float[]{1.0f - 0.45f * f, 1.0f - 0.15f * f, 1.0f};
    }

    // ---------------------------------------------------------------- beam reflection & absorption

    /** True while a shield or mirror bounces beams back at whoever fired them. */
    public boolean reflectsBeams() {
        return false;
    }

    public static boolean isBeam(DamageSource source) {
        return source.is(com.reiko45.godzilla.registry.ModDamageTypes.KAIJU_BEAM);
    }

    /** Called when a beam is reflected. Return the damage multiplier sent back. */
    protected float onBeamReflected(DamageSource source, float amount) {
        return 1.0f;
    }

    /** Lets a kaiju soak up part of a beam (Heisei Mechagodzilla's Plasma Grenade). Returns damage still taken. */
    protected float absorbBeam(DamageSource source, float amount) {
        return amount;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide && isBeam(source) && source.getEntity() instanceof LivingEntity attacker && attacker != this) {
            if (reflectsBeams()) {
                float multiplier = onBeamReflected(source, amount);
                if (level() instanceof ServerLevel server) {
                    server.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, getX(), getY(0.6), getZ(), 20,
                            getBbWidth() * 0.5, getBbHeight() * 0.3, getBbWidth() * 0.5, 0.2);
                    KaijuDestruction.hurtIgnoringCooldown(attacker, new DamageSource(source.typeHolder(), this), amount * multiplier);
                }
                return false;
            }
            amount = absorbBeam(source, amount);
            if (amount <= 0) return false;
        }
        if (isFrozenSolid()) amount *= 1.5f;
        if (source.is(DamageTypeTags.IS_FIRE) && getFreeze() > 0) entityData.set(DATA_FREEZE, Math.max(0f, getFreeze() - amount));
        return super.hurt(source, amount);
    }

    // ---------------------------------------------------------------- piloting (mechs, ridden kaiju)

    /** Whether {@code player} can steer this kaiju when riding it. */
    public boolean canBePilotedBy(Player player) {
        return false;
    }

    /** Pilots of big mechs sit in an armoured cockpit: they can't be hurt while inside. */
    public boolean protectsPilot() {
        return false;
    }

    /** Hide the pilot model (they're inside the machine). */
    public boolean hidesPilot() {
        return protectsPilot();
    }

    /** Ability index fired by a pilot key, or -1. */
    protected int pilotAbility(int action) {
        return -1;
    }

    public boolean showsBossBar() {
        return getFaction() != KaijuFaction.HUMAN;
    }

    @Nullable
    public Player getPilot() {
        return getControllingPassenger() instanceof Player p ? p : null;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (getFirstPassenger() instanceof Player player && canBePilotedBy(player)) return player;
        return super.getControllingPassenger();
    }

    @Override
    public void vehicleAction(ServerPlayer rider, int action) {
        if (getPilot() != rider) return;
        onPilotAction(rider, action);
    }

    protected void onPilotAction(ServerPlayer pilot, int action) {
        int index = pilotAbility(action);
        if (index >= 0) tryPilotAbility(index, pilot);
    }

    protected boolean tryPilotAbility(int index, ServerPlayer pilot) {
        if (isAbilityActive() || isFrozenSolid()) return false;
        KaijuAbility ability = abilities.get(index);
        if (!ability.isReady(this)) {
            pilot.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.reikogodzilla.recharging",
                    net.minecraft.network.chat.Component.translatable("ability.reikogodzilla." + ability.name)), true);
            return false;
        }
        startAbility(index, null);
        return true;
    }

    /** Where the pilot is aiming: the first block along their view, up to 160 blocks away. */
    @Nullable
    public Vec3 getPilotAimPoint() {
        Player pilot = getPilot();
        if (pilot == null) return null;
        Vec3 eye = pilot.getEyePosition();
        Vec3 far = eye.add(pilot.getLookAngle().scale(160));
        net.minecraft.world.phys.BlockHitResult hit = level().clip(new net.minecraft.world.level.ClipContext(eye, far,
                net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, pilot));
        return hit.getType() == net.minecraft.world.phys.HitResult.Type.MISS ? far : hit.getLocation();
    }

    @Override
    protected void tickRidden(Player player, Vec3 input) {
        super.tickRidden(player, input);
        float yaw = net.minecraft.util.Mth.approachDegrees(getYRot(), player.getYRot(), pilotTurnSpeed());
        setRot(yaw, player.getXRot() * 0.5f);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
    }

    protected float pilotTurnSpeed() {
        return 10f;
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 input) {
        float strafe = player.xxa * 0.5f;
        float forward = player.zza;
        if (forward <= 0) forward *= 0.4f;
        return new Vec3(strafe, 0, forward);
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return (float) getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.9f;
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
        tag.putFloat("Freeze", getFreeze());
        tag.putInt("FrozenTicks", frozenTicks);
        tag.putInt("Cadmium", cadmiumTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_VARIANT, tag.getInt("Variant"));
        destination = NbtUtils.readBlockPos(tag, "Destination").orElse(null);
        regenSuppressedTicks = tag.getInt("RegenSuppressed");
        entityData.set(DATA_FREEZE, tag.getFloat("Freeze"));
        frozenTicks = tag.getInt("FrozenTicks");
        entityData.set(DATA_FROZEN, frozenTicks > 0);
        cadmiumTicks = tag.getInt("Cadmium");
        entityData.set(DATA_CADMIUM, cadmiumTicks > 0);
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

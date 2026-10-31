package com.reiko45.godzilla.entity.vehicle;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.*;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModItems;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.Comparator;

/**
 * Type 66 Maser Cannon tank. Ride it (use), drive with W/S, turn with A/D, and press the Fire Weapon key (R) to fire
 * a maser beam where you are looking. Unmanned tanks guard the area and fire at hostile kaiju on their own.
 * Sneak + use to pack it back into an item; use an iron ingot to repair it.
 */
public class MaserTankEntity extends PathfinderMob implements GeoEntity, BeamEmitter, KaijuAligned, VehicleWeapon {
    private static final EntityDataAccessor<Integer> DATA_FIRING = SynchedEntityData.defineId(MaserTankEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Vector3f> DATA_BEAM_END = SynchedEntityData.defineId(MaserTankEntity.class, EntityDataSerializers.VECTOR3);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation DRIVE = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation FIRE = RawAnimation.begin().thenLoop("beam");
    private static final int FIRE_TICKS = 30;
    private static final int COOLDOWN = 60;
    private static final double RANGE = 96;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int cooldown;
    @Nullable
    private LivingEntity autoTarget;

    public MaserTankEntity(EntityType<? extends MaserTankEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 120)
                .add(Attributes.ARMOR, 15)
                .add(Attributes.ARMOR_TOUGHNESS, 4)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.STEP_HEIGHT, 1.1)
                .add(Attributes.FOLLOW_RANGE, 64);
    }

    @Override
    protected void registerGoals() {}

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FIRING, 0);
        builder.define(DATA_BEAM_END, new Vector3f());
    }

    @Override
    public KaijuFaction getFaction() {
        return KaijuFaction.HUMAN;
    }

    // ------------------------------------------------------------------ weapon

    @Override
    public void vehicleAction(ServerPlayer rider, int action) {
        if (action == 0 && getControllingPassenger() == rider) startFiring();
    }

    private void startFiring() {
        if (cooldown > 0 || entityData.get(DATA_FIRING) > 0) return;
        entityData.set(DATA_FIRING, FIRE_TICKS);
        cooldown = COOLDOWN + FIRE_TICKS;
        level().playSound(null, getX(), getY(), getZ(), ModSounds.MASER_FIRE.get(), SoundSource.NEUTRAL, 4f, 1f);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (cooldown > 0) cooldown--;
        int firing = entityData.get(DATA_FIRING);
        if (firing > 0) {
            tickBeam(FIRE_TICKS - firing);
            entityData.set(DATA_FIRING, firing - 1);
        } else if (getControllingPassenger() == null && GodzillaConfig.get(GodzillaConfig.MASER_AUTO_FIRE, true) && tickCount % 10 == 0) {
            autoTarget = findAutoTarget();
            if (autoTarget != null) {
                turnTowards(autoTarget.position(), 30f);
                if (cooldown <= 0) startFiring();
            }
        }
    }

    @Nullable
    private LivingEntity findAutoTarget() {
        return level().getEntitiesOfClass(KaijuEntity.class, getBoundingBox().inflate(RANGE * 0.66),
                        k -> k.isAlive() && isAutoTarget(k) && hasLineOfSight(k))
                .stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
    }

    private void turnTowards(Vec3 point, float maxDegrees) {
        float yaw = (float) (Mth.atan2(point.z - getZ(), point.x - getX()) * (180.0 / Math.PI)) - 90.0f;
        setYRot(Mth.approachDegrees(getYRot(), yaw, maxDegrees));
        yBodyRot = yHeadRot = getYRot();
    }

    private void tickBeam(int tick) {
        if (!(level() instanceof ServerLevel server)) return;
        Vec3 origin = getBeamOrigin(0, 1f);
        Vec3 dir;
        LivingEntity rider = getControllingPassenger();
        if (rider != null) {
            dir = rider.getLookAngle();
        } else if (autoTarget != null && autoTarget.isAlive()) {
            dir = autoTarget.getBoundingBox().getCenter().subtract(origin).normalize();
            turnTowards(autoTarget.position(), 6f);
        } else {
            dir = Vec3.directionFromRotation(0, getYRot());
        }
        Vec3 far = origin.add(dir.scale(RANGE));
        BlockHitResult hit = server.clip(new ClipContext(origin, far, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        Vec3 end = hit.getType() == HitResult.Type.MISS ? far : hit.getLocation();
        entityData.set(DATA_BEAM_END, new Vector3f((float) end.x, (float) end.y, (float) end.z));
        if (tick % 3 == 0) {
            KaijuDestruction.damageAlongSegment(this, origin, end, 0.6, ModDamageTypes.source(server, damageType(), rider != null ? rider : this),
                    pulseDamage(), e -> e != rider && !hasPassenger(e) && !(e instanceof Player), this::onPulseHit);
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK, end.x, end.y, end.z, 12, 0.4, 0.4, 0.4, 0.2);
        }
    }

    /** Which kaiju an unmanned tank shoots at. */
    protected boolean isAutoTarget(KaijuEntity k) {
        return k.getFaction() == KaijuFaction.INVADER || k.getTarget() instanceof Player || k.getTarget() instanceof KaijuAligned a && a.getFaction() == KaijuFaction.HUMAN;
    }

    /** Damage per pulse (every 3 ticks). */
    protected float pulseDamage() {
        return (float) GodzillaConfig.get(GodzillaConfig.MASER_DAMAGE, 8.0);
    }

    protected net.minecraft.resources.ResourceKey<net.minecraft.world.damagesource.DamageType> damageType() {
        return ModDamageTypes.MASER;
    }

    /** Extra effect on each thing the beam hits. */
    protected void onPulseHit(LivingEntity target) {}

    /** The item this vehicle packs back into. */
    protected net.minecraft.world.item.Item packedItem() {
        return ModItems.MASER_TANK.get();
    }

    // ------------------------------------------------------------------ BeamEmitter

    @Override
    public int getActiveBeamCount() {
        return entityData.get(DATA_FIRING) > 0 ? 1 : 0;
    }

    @Override
    public Vec3 getBeamOrigin(int index, float partialTick) {
        float yaw = Mth.rotLerp(partialTick, yBodyRotO, yBodyRot) * Mth.DEG_TO_RAD;
        return getPosition(partialTick).add(new Vec3(0, 2.1, 1.7).scale(getScale()).yRot(-yaw));
    }

    @Override
    public Vec3 getBeamEnd(int index) {
        Vector3f v = entityData.get(DATA_BEAM_END);
        return new Vec3(v.x, v.y, v.z);
    }

    @Override
    public BeamStyle getBeamStyle() {
        return BeamStyle.MASER;
    }

    @Override
    public float getBeamWidth() {
        return 0.22f;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        AABB box = getBoundingBox().inflate(2);
        return getActiveBeamCount() > 0 ? box.minmax(new AABB(getBeamEnd(0), getBeamEnd(0)).inflate(2)) : box;
    }

    // ------------------------------------------------------------------ riding & interaction

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.IRON_INGOT) && getHealth() < getMaxHealth()) {
            stack.consume(1, player);
            heal(25);
            playSound(SoundEvents.IRON_GOLEM_REPAIR, 1f, 1f);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (player.isSecondaryUseActive() && !isVehicle()) {
            if (!level().isClientSide) {
                if (!player.getAbilities().instabuild) player.getInventory().placeItemBackInInventory(new ItemStack(packedItem()));
                discard();
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (!isVehicle()) {
            if (!level().isClientSide) player.startRiding(this);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return getFirstPassenger() instanceof Player p ? p : null;
    }

    @Override
    protected void tickRidden(Player player, Vec3 input) {
        super.tickRidden(player, input);
        float turn = -player.xxa * 4.0f;
        setYRot(getYRot() + turn);
        this.yRotO = this.yBodyRot = this.yHeadRot = getYRot();
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 input) {
        float forward = player.zza > 0 ? 1.0f : player.zza < 0 ? -0.5f : 0f;
        return new Vec3(0, 0, forward);
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return (float) getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(DamageTypes.DROWN) || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CACTUS) || super.isInvulnerableTo(source);
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
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_EXPLODE.value();
    }

    @Override
    protected void tickDeath() {
        if (deathTime == 0 && level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(0.5), getZ(), 1, 0, 0, 0, 0);
        }
        super.tickDeath();
    }

    // ------------------------------------------------------------------ GeckoLib

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> {
            if (getActiveBeamCount() > 0) return state.setAndContinue(FIRE);
            return state.setAndContinue(state.isMoving() ? DRIVE : IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}

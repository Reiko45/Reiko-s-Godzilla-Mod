package com.reiko45.godzilla.entity.kaiju;

import com.reiko45.godzilla.entity.BeamEmitter;
import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.KaijuAligned;
import com.reiko45.godzilla.entity.KaijuFaction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * A crystal tower raised by SpaceGodzilla. While it stands it channels energy into its creator (drawn as a beam).
 * Smash the crystals to cut off SpaceGodzilla's power. They shatter when their creator dies.
 */
public class SpaceCrystalEntity extends Mob implements GeoEntity, BeamEmitter, KaijuAligned {
    private static final EntityDataAccessor<Integer> DATA_OWNER_ID = SynchedEntityData.defineId(SpaceCrystalEntity.class, EntityDataSerializers.INT);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    @Nullable
    private UUID ownerUuid;
    private int orphanTicks;

    public SpaceCrystalEntity(EntityType<? extends SpaceCrystalEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 80).add(Attributes.ARMOR, 10)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_OWNER_ID, -1);
    }

    public void setOwner(Entity owner) {
        ownerUuid = owner.getUUID();
        entityData.set(DATA_OWNER_ID, owner.getId());
    }

    @Nullable
    private Entity owner() {
        int id = entityData.get(DATA_OWNER_ID);
        return id >= 0 ? level().getEntity(id) : null;
    }

    @Override
    public KaijuFaction getFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(0, getDeltaMovement().y, 0);
        if (!(level() instanceof ServerLevel server) || tickCount % 20 != 0) return;
        Entity owner = ownerUuid != null ? server.getEntity(ownerUuid) : null;
        if (owner instanceof LivingEntity living && living.isAlive() && owner.distanceToSqr(this) < 96 * 96) {
            orphanTicks = 0;
            if (entityData.get(DATA_OWNER_ID) != owner.getId()) entityData.set(DATA_OWNER_ID, owner.getId());
        } else {
            entityData.set(DATA_OWNER_ID, -1);
            if ((orphanTicks += 20) > 200) shatter();
        }
    }

    /** True while this crystal is feeding power to a SpaceGodzilla. */
    public boolean isPowering(Entity kaiju) {
        return entityData.get(DATA_OWNER_ID) == kaiju.getId();
    }

    private void shatter() {
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.END_ROD, getX(), getY(0.5), getZ(), 40, 1, 2, 1, 0.15);
        }
        playSound(SoundEvents.AMETHYST_BLOCK_BREAK, 4f, 0.6f);
        discard();
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        playSound(SoundEvents.AMETHYST_BLOCK_BREAK, 4f, 0.6f);
    }

    // ---- energy link drawn to the owner
    @Override
    public int getActiveBeamCount() {
        return owner() != null ? 1 : 0;
    }

    @Override
    public Vec3 getBeamOrigin(int index, float partialTick) {
        return getPosition(partialTick).add(0, getBbHeight() * 0.9, 0);
    }

    @Override
    public Vec3 getBeamEnd(int index) {
        Entity owner = owner();
        return owner == null ? position() : owner.position().add(0, owner.getBbHeight() * 0.6, 0);
    }

    @Override
    public BeamStyle getBeamStyle() {
        return BeamStyle.CORONA;
    }

    @Override
    public float getBeamWidth() {
        return 0.12f;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        AABB box = getBoundingBox().inflate(2);
        return getActiveBeamCount() > 0 ? box.minmax(new AABB(getBeamEnd(0), getBeamEnd(0)).inflate(4)) : box;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.AMETHYST_BLOCK_HIT;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUuid != null) tag.putUUID("CrystalOwner", ownerUuid);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ownerUuid = tag.hasUUID("CrystalOwner") ? tag.getUUID("CrystalOwner") : null;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, state -> state.setAndContinue(IDLE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}

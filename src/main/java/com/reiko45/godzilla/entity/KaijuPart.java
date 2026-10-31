package com.reiko45.godzilla.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;

import javax.annotation.Nullable;

/**
 * An extra hitbox (head, tail, wing) attached to a kaiju. Damage is forwarded to the parent, with a multiplier
 * so that, for example, head shots hurt more than tail hits.
 */
public class KaijuPart extends PartEntity<KaijuEntity> {
    public final String name;
    /** Offset from the kaiju's feet at scale 1: x = left, y = up, z = forward. */
    public final Vec3 offset;
    public final float damageMultiplier;
    private final EntityDimensions baseSize;

    public KaijuPart(KaijuEntity parent, String name, float width, float height, Vec3 offset, float damageMultiplier) {
        super(parent);
        this.name = name;
        this.offset = offset;
        this.damageMultiplier = damageMultiplier;
        this.baseSize = EntityDimensions.scalable(width, height);
        this.refreshDimensions();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public boolean isPickable() {
        return getParent().isAlive();
    }

    @Nullable
    @Override
    public ItemStack getPickResult() {
        return getParent().getPickResult();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return !this.isInvulnerableTo(source) && getParent().hurtFromPart(this, source, amount);
    }

    /** Commands like /kill @e[type=...] also select parts (they share the parent's type), so pass it on. */
    @Override
    public void kill() {
        getParent().kill();
    }

    @Override
    public boolean is(Entity entity) {
        return this == entity || getParent() == entity;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        KaijuEntity parent = getParent();
        return parent == null ? baseSize : baseSize.scale(parent.getScale());
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}

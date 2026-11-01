package com.reiko45.godzilla.entity.projectile;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.registry.ModItems;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * An unmanned train packed with explosives (Shin Godzilla's Operation Yashiori). It runs along the ground toward the
 * kaiju it was sent at, climbs its legs and blows up. Each hit knocks Shin Godzilla closer to the ground.
 */
public class TrainBombEntity extends ThrowableItemProjectile {
    @Nullable
    private UUID targetId;
    private int life;

    public TrainBombEntity(EntityType<? extends TrainBombEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public TrainBombEntity(Level level, LivingEntity owner, @Nullable Entity target) {
        super(ModEntities.TRAIN_BOMB.get(), owner, level);
        setNoGravity(true);
        if (target != null) targetId = (target instanceof KaijuPart p ? p.getParent() : target).getUUID();
    }

    @Override
    protected Item getDefaultItem() {
        return Items.TNT_MINECART;
    }

    @Override
    public void tick() {
        if (!level().isClientSide) {
            life++;
            Entity target = targetId != null && level() instanceof ServerLevel s ? s.getEntity(targetId) : null;
            Vec3 dir = target != null ? target.position().subtract(position()).multiply(1, 0, 1) : getDeltaMovement().multiply(1, 0, 1);
            if (dir.lengthSqr() < 1.0E-4) dir = Vec3.directionFromRotation(0, getYRot());
            dir = dir.normalize().scale(0.9);
            double nx = getX() + dir.x, nz = getZ() + dir.z;
            double ground = level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) Math.floor(nx), (int) Math.floor(nz)) + 0.6;
            double dy = ground - getY();
            if (target != null && target.getBoundingBox().inflate(3, 0, 3).contains(nx, target.getY() + 1, nz)) {
                // Climb the target.
                dy = target.getBoundingBox().getCenter().y - getY();
                dy = Math.signum(dy) * Math.min(Math.abs(dy), 0.8);
            }
            setDeltaMovement(dir.x, Math.max(-1.5, Math.min(1.2, dy)), dir.z);
            setYRot((float) Math.toDegrees(Math.atan2(-dir.x, dir.z)));
            if (life % 4 == 0) level().playSound(null, getX(), getY(), getZ(), SoundEvents.MINECART_RIDING, SoundSource.NEUTRAL, 0.6f, 0.8f);
            if (life > 400) detonate(null);
        }
        super.tick();
        if (level().isClientSide) level().addParticle(ParticleTypes.SMOKE, getX(), getY() + 0.5, getZ(), 0, 0.05, 0);
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        Entity t = target instanceof KaijuPart p ? p.getParent() : target;
        return t instanceof KaijuEntity k && k.getFaction() != KaijuFaction.HUMAN && super.canHitEntity(target);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity e = result.getEntity();
        detonate(e instanceof KaijuPart p ? p.getParent() : e instanceof KaijuEntity k ? k : null);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (result.getDirection().getAxis().isHorizontal()) detonate(null);
    }

    private void detonate(@Nullable KaijuEntity kaiju) {
        if (!(level() instanceof ServerLevel server) || isRemoved()) return;
        boolean grief = getOwner() != null && KaijuDestruction.canGrief(server, getOwner());
        server.explode(this, getX(), getY(), getZ(), 4f, grief ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);
        if (kaiju != null) {
            kaiju.hurt(server.damageSources().explosion(this, getOwner()), 30f);
            if (kaiju instanceof com.reiko45.godzilla.entity.reiwa.ShinGodzillaEntity shin) shin.addKnockdown(0.35f);
        }
        discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (targetId != null) tag.putUUID("Target", targetId);
        tag.putInt("Life", life);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        targetId = tag.hasUUID("Target") ? tag.getUUID("Target") : null;
        life = tag.getInt("Life");
    }
}

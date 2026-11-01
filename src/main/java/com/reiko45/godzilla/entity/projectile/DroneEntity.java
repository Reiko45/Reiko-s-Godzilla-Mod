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
 * A cheap unmanned drone (Shin Godzilla). Sent up in waves, they circle a kaiju and fire small rounds at it; their
 * real job is to bait Shin Godzilla's dorsal beams and drain its energy. They break down after half a minute.
 */
public class DroneEntity extends ThrowableItemProjectile {
    private int life;
    private double angle;

    public DroneEntity(EntityType<? extends DroneEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public DroneEntity(Level level, LivingEntity owner) {
        super(ModEntities.DRONE.get(), owner, level);
        setNoGravity(true);
        angle = level.getRandom().nextDouble() * Math.PI * 2;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.DRONE_SWARM.get();
    }

    @Override
    public void tick() {
        if (level() instanceof ServerLevel server) {
            if (++life > 600) {
                server.sendParticles(ParticleTypes.SMOKE, getX(), getY(), getZ(), 6, 0.2, 0.2, 0.2, 0.02);
                discard();
                return;
            }
            KaijuEntity target = null;
            double best = 96 * 96;
            for (KaijuEntity k : server.getEntitiesOfClass(KaijuEntity.class, getBoundingBox().inflate(96), k -> k.isAlive() && k.getFaction() != KaijuFaction.HUMAN)) {
                double d = k.distanceToSqr(this);
                if (d < best) {
                    best = d;
                    target = k;
                }
            }
            if (target != null) {
                angle += 0.06;
                double r = 8 + target.getBbWidth();
                Vec3 goal = target.position().add(Math.cos(angle) * r, target.getBbHeight() * 0.85 + 3, Math.sin(angle) * r);
                Vec3 v = goal.subtract(position());
                if (v.length() > 1.2) v = v.normalize().scale(1.2);
                setDeltaMovement(v);
                if (life % 40 == 0) {
                    var round = new KaijuMissileEntity(server, getX(), getY(), getZ(), KaijuMissileEntity.Payload.RAILGUN);
                    if (getOwner() != null) round.setOwner(getOwner());
                    Vec3 aim = target.getBoundingBox().getCenter().subtract(position()).normalize();
                    round.shoot(aim.x, aim.y, aim.z, 2.0f, 2f);
                    server.addFreshEntity(round);
                }
            } else {
                setDeltaMovement(getDeltaMovement().scale(0.9).add(0, 0.01, 0));
            }
            if (life % 10 == 0) server.playSound(null, getX(), getY(), getZ(), SoundEvents.BEE_LOOP, SoundSource.NEUTRAL, 0.5f, 2.0f);
        }
        super.tick();
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return false;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        setDeltaMovement(getDeltaMovement().add(0, 0.4, 0));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Life", life);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        life = tag.getInt("Life");
    }
}

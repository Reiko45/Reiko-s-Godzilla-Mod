package com.reiko45.godzilla.entity.projectile;

import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** A small explosive rocket (Mechagodzilla's finger missiles). */
public class KaijuMissileEntity extends ThrowableItemProjectile {
    public KaijuMissileEntity(EntityType<? extends KaijuMissileEntity> type, Level level) {
        super(type, level);
    }

    public KaijuMissileEntity(Level level, LivingEntity shooter) {
        super(ModEntities.KAIJU_MISSILE.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.FIRE_CHARGE;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            level().addParticle(ParticleTypes.SMOKE, getX(), getY(), getZ(), 0, 0, 0);
            level().addParticle(ParticleTypes.FLAME, getX(), getY(), getZ(), 0, 0, 0);
        } else if (tickCount > 120) {
            discard();
        }
    }

    @Override
    protected boolean canHitEntity(net.minecraft.world.entity.Entity target) {
        if (target instanceof net.neoforged.neoforge.entity.PartEntity<?> part && part.getParent() == getOwner()) return false;
        return super.canHitEntity(target);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        result.getEntity().hurt(damageSources().explosion(this, getOwner()), 6.0f);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level().isClientSide) return;
        boolean grief = getOwner() != null && KaijuDestruction.canGrief(level(), getOwner());
        level().explode(this, getX(), getY(), getZ(), 2.0f, false, grief ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
        discard();
    }
}

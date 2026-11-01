package com.reiko45.godzilla.entity.projectile;

import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Something big and heavy flying through the air: Suko's boulders, Apex Mechagodzilla's rocket fist. Hits hard and
 * can burst on impact.
 */
public class HeavyProjectileEntity extends ThrowableItemProjectile {
    private float damage = 10f;
    private float blast = 0f;

    public HeavyProjectileEntity(EntityType<? extends HeavyProjectileEntity> type, Level level) {
        super(type, level);
    }

    public HeavyProjectileEntity(Level level, LivingEntity shooter, ItemStack look, float damage, float blast) {
        super(ModEntities.HEAVY_PROJECTILE.get(), shooter, level);
        setItem(look);
        this.damage = damage;
        this.blast = blast;
    }

    @Override
    protected Item getDefaultItem() {
        return Items.COBBLESTONE;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity owner = getOwner();
        if (result.getEntity() == owner || owner != null && result.getEntity().isPassengerOfSameVehicle(owner)) return;
        result.getEntity().hurt(damageSources().thrown(this, owner), damage);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!(level() instanceof ServerLevel server)) return;
        if (result instanceof EntityHitResult e && (e.getEntity() == getOwner())) return;
        if (blast > 0) {
            boolean grief = getOwner() != null && KaijuDestruction.canGrief(server, getOwner());
            server.explode(this, getX(), getY(), getZ(), blast, false, grief ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
        }
        if (getItem().getItem() instanceof BlockItem block) {
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, block.getBlock().defaultBlockState()), getX(), getY(), getZ(), 30, 0.6, 0.6, 0.6, 0.1);
        }
        discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Damage", damage);
        tag.putFloat("Blast", blast);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        damage = tag.getFloat("Damage");
        blast = tag.getFloat("Blast");
    }
}

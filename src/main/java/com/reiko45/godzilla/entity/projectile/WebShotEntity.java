package com.reiko45.godzilla.entity.projectile;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.entity.PartEntity;

/** Kumonga's sticky thread (binds its prey and leaves cobwebs) and its venom needle (strong poison). */
public class WebShotEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Boolean> DATA_NEEDLE = SynchedEntityData.defineId(WebShotEntity.class, EntityDataSerializers.BOOLEAN);

    public WebShotEntity(EntityType<? extends WebShotEntity> type, Level level) {
        super(type, level);
    }

    public WebShotEntity(Level level, LivingEntity shooter, boolean needle) {
        super(ModEntities.WEB_SHOT.get(), shooter, level);
        entityData.set(DATA_NEEDLE, needle);
        setItem(new ItemStack(needle ? Items.ARROW : Items.COBWEB));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_NEEDLE, false);
    }

    public boolean isNeedle() {
        return entityData.get(DATA_NEEDLE);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.COBWEB;
    }

    @Override
    protected double getDefaultGravity() {
        return isNeedle() ? 0.005 : 0.02;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) level().addParticle(isNeedle() ? ParticleTypes.CRIT : ParticleTypes.WHITE_ASH, getX(), getY(), getZ(), 0, 0, 0);
        else if (tickCount > 100) discard();
    }

    @Override
    protected boolean canHitEntity(net.minecraft.world.entity.Entity target) {
        if (target instanceof PartEntity<?> part && part.getParent() == getOwner()) return false;
        return super.canHitEntity(target);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        net.minecraft.world.entity.Entity hit = result.getEntity() instanceof PartEntity<?> part ? part.getParent() : result.getEntity();
        if (hit == getOwner() || !(hit instanceof LivingEntity target)) return;
        LivingEntity owner = getOwner() instanceof LivingEntity l ? l : null;
        if (isNeedle()) {
            target.hurt(damageSources().mobProjectile(this, owner), 7f);
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 140, 2));
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
        } else {
            target.hurt(damageSources().mobProjectile(this, owner), 2f);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, target instanceof KaijuEntity ? 2 : 4));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
            if (!(target instanceof KaijuEntity)) web(target.blockPosition());
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!isNeedle()) web(result.getBlockPos().relative(result.getDirection()));
    }

    private void web(BlockPos pos) {
        if (!(level() instanceof ServerLevel server) || getOwner() == null || !KaijuDestruction.canGrief(server, getOwner())) return;
        if (server.getBlockState(pos).isAir()) server.setBlockAndUpdate(pos, Blocks.COBWEB.defaultBlockState());
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(isNeedle() ? ParticleTypes.CRIT : ParticleTypes.WHITE_ASH, getX(), getY(), getZ(), 10, 0.3, 0.3, 0.3, 0.05);
            discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Needle", isNeedle());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_NEEDLE, tag.getBoolean("Needle"));
    }
}

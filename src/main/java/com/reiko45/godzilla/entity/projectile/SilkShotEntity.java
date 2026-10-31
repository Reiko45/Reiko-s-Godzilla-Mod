package com.reiko45.godzilla.entity.projectile;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** A glob of Mothra larva silk: slows and entangles whatever it hits. */
public class SilkShotEntity extends ThrowableItemProjectile {
    public SilkShotEntity(EntityType<? extends SilkShotEntity> type, Level level) {
        super(type, level);
    }

    public SilkShotEntity(Level level, LivingEntity shooter) {
        super(ModEntities.SILK_SHOT.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.COBWEB;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.01;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide && tickCount % 2 == 0) {
            level().addParticle(ParticleTypes.WHITE_ASH, getX(), getY(), getZ(), 0, 0, 0);
        }
        if (tickCount > 100) discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() == getOwner() || !(result.getEntity() instanceof LivingEntity target)) return;
        target.hurt(damageSources().thrown(this, getOwner()), 3.0f);
        boolean big = target instanceof KaijuEntity;
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, big ? 200 : 100, big ? 2 : 3));
        if (!big && !level().isClientSide && getOwner() != null && KaijuDestruction.canGrief(level(), getOwner())) {
            BlockPos feet = target.blockPosition();
            if (level().getBlockState(feet).isAir()) level().setBlockAndUpdate(feet, Blocks.COBWEB.defaultBlockState());
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide) discard();
    }
}

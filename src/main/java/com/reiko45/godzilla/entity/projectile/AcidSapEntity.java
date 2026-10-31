package com.reiko45.godzilla.entity.projectile;

import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Biollante's radioactive sap: corrodes armour and flesh, and eats away a few blocks where it lands. */
public class AcidSapEntity extends ThrowableItemProjectile {
    public AcidSapEntity(EntityType<? extends AcidSapEntity> type, Level level) {
        super(type, level);
    }

    public AcidSapEntity(Level level, LivingEntity shooter) {
        super(ModEntities.ACID_SAP.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SLIME_BALL;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) level().addParticle(ParticleTypes.ITEM_SLIME, getX(), getY(), getZ(), 0, 0, 0);
        else if (tickCount > 120) discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() == getOwner() || !(result.getEntity() instanceof LivingEntity target)) return;
        target.hurt(ModDamageTypes.source(level(), ModDamageTypes.ACID_SAP, getOwner()), 7.0f);
        target.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (level() instanceof ServerLevel server && getOwner() != null && KaijuDestruction.canGrief(server, getOwner()) && random.nextInt(3) == 0) {
            BlockPos pos = result.getBlockPos();
            BlockState state = server.getBlockState(pos);
            if (KaijuDestruction.isBreakable(server, pos, state) && state.getDestroySpeed(server, pos) < 3.0f) {
                server.levelEvent(2001, pos, net.minecraft.world.level.block.Block.getId(state));
                server.removeBlock(pos, false);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.ITEM_SLIME, getX(), getY(), getZ(), 12, 0.4, 0.4, 0.4, 0.1);
            discard();
        }
    }
}

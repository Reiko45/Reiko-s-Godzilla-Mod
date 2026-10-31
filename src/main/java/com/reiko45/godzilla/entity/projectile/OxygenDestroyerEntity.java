package com.reiko45.godzilla.entity.projectile;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Dr. Serizawa's Oxygen Destroyer (1954). Once it sinks into water it splits the oxygen atoms around it: every living
 * thing in the water nearby dies, and even Godzilla is reduced to bones. Ten pulses over five seconds; kaiju lose a
 * configurable share of their health per pulse and can't regenerate for two minutes.
 */
public class OxygenDestroyerEntity extends ThrowableItemProjectile {
    private static final int PULSES = 10;
    private static final double RADIUS = 24;
    private int activeTicks = -1;

    public OxygenDestroyerEntity(EntityType<? extends OxygenDestroyerEntity> type, Level level) {
        super(type, level);
    }

    public OxygenDestroyerEntity(Level level, LivingEntity thrower) {
        super(ModEntities.OXYGEN_DESTROYER.get(), thrower, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.OXYGEN_DESTROYER.get();
    }

    public boolean isActive() {
        return activeTicks >= 0;
    }

    @Override
    public void tick() {
        if (isActive()) {
            tickActive();
            return;
        }
        super.tick();
        if (!level().isClientSide && isInWater()) activate();
        if (!level().isClientSide && tickCount > 400) discard();
    }

    private void activate() {
        activeTicks = 0;
        setDeltaMovement(0, -0.02, 0);
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.CONDUIT_ACTIVATE, SoundSource.PLAYERS, 4f, 0.5f);
        if (level() instanceof ServerLevel server) {
            for (Player p : server.players()) {
                if (p.distanceToSqr(this) < 96 * 96) p.displayClientMessage(Component.translatable("message.reikogodzilla.oxygen_destroyer"), true);
            }
        }
    }

    private void tickActive() {
        activeTicks++;
        setPos(getX(), getY() - 0.01, getZ());
        if (!(level() instanceof ServerLevel server)) return;
        server.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, getX(), getY(), getZ(), 30, 2, 1, 2, 0.2);
        server.sendParticles(ParticleTypes.BUBBLE_POP, getX(), getY(), getZ(), 20, RADIUS * 0.4, 2, RADIUS * 0.4, 0.05);
        if (activeTicks % 10 == 0) pulse(server);
        if (activeTicks >= PULSES * 10) discard();
    }

    private void pulse(ServerLevel server) {
        server.playSound(null, getX(), getY(), getZ(), SoundEvents.CONDUIT_ATTACK_TARGET, SoundSource.PLAYERS, 3f, 0.6f);
        DamageSource source = ModDamageTypes.source(server, ModDamageTypes.OXYGEN_DESTROYER, getOwner());
        double fraction = GodzillaConfig.get(GodzillaConfig.OXYGEN_DESTROYER_KAIJU_DAMAGE, 0.09);
        for (LivingEntity e : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(RADIUS), LivingEntity::isInWater)) {
            if (e instanceof Player p && (p.isCreative() || p.isSpectator())) continue;
            e.invulnerableTime = 0;
            if (e instanceof KaijuEntity kaiju) {
                kaiju.suppressRegeneration(2400);
                kaiju.hurt(source, (float) (kaiju.getMaxHealth() * fraction));
            } else {
                e.hurt(source, Math.max(100f, e.getMaxHealth() * 2));
            }
            server.sendParticles(ParticleTypes.BUBBLE, e.getX(), e.getY(0.5), e.getZ(), 15, e.getBbWidth() * 0.5, e.getBbHeight() * 0.5, e.getBbWidth() * 0.5, 0.1);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        // Passes through: it only works once it is in the water.
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (level().isClientSide || isActive()) return;
        if (isInWater()) {
            activate();
        } else {
            spawnAtLocation(getItem());
            discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ActiveTicks", activeTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        activeTicks = tag.contains("ActiveTicks") ? tag.getInt("ActiveTicks") : -1;
    }
}

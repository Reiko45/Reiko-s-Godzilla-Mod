package com.reiko45.godzilla.entity.projectile;

import com.reiko45.godzilla.entity.showa.VaranEntity;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.registry.ModItems;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A Light Bomb (Varan the Unbelievable, 1958): a magnesium flare packed with Special Gunpowder. Where it lands it
 * burns brightly for fifteen seconds. Varan can't resist light: it dives for the flare and swallows it, and the charge
 * goes off inside. Left alone, the flare finally bursts on its own.
 */
public class LightBombEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Boolean> DATA_LIT = SynchedEntityData.defineId(LightBombEntity.class, EntityDataSerializers.BOOLEAN);
    private int litTicks;

    public LightBombEntity(EntityType<? extends LightBombEntity> type, Level level) {
        super(type, level);
    }

    public LightBombEntity(Level level, LivingEntity thrower) {
        super(ModEntities.LIGHT_BOMB.get(), thrower, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_LIT, false);
    }

    public boolean isLit() {
        return entityData.get(DATA_LIT);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.LIGHT_BOMB.get();
    }

    @Override
    public void tick() {
        if (!isLit()) {
            super.tick();
            return;
        }
        baseTick();
        setDeltaMovement(Vec3.ZERO);
        if (level().isClientSide) {
            level().addParticle(ParticleTypes.FLAME, getX(), getY() + 0.2, getZ(), (random.nextDouble() - 0.5) * 0.05, 0.08, (random.nextDouble() - 0.5) * 0.05);
            level().addParticle(ParticleTypes.END_ROD, getX(), getY() + 0.3, getZ(), (random.nextDouble() - 0.5) * 0.1, 0.1, (random.nextDouble() - 0.5) * 0.1);
            return;
        }
        if (litTicks % 20 == 0) level().playSound(null, getX(), getY(), getZ(), SoundEvents.FIRE_AMBIENT, SoundSource.NEUTRAL, 2f, 1.4f);
        if (++litTicks > 300) burst();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level().isClientSide || isLit()) return;
        // Lands and lights up.
        Vec3 at = result.getLocation();
        setPos(at.x, at.y + 0.1, at.z);
        setNoGravity(true);
        entityData.set(DATA_LIT, true);
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 2f, 1.2f);
    }

    /** Varan gulps it down. */
    public void swallow(VaranEntity varan) {
        if (!(level() instanceof ServerLevel server) || isRemoved()) return;
        server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, varan.getX(), varan.getY(0.5), varan.getZ(), 1, 0, 0, 0, 0);
        server.playSound(null, varan.getX(), varan.getY(), varan.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 8f, 0.6f);
        KaijuDestruction.hurtIgnoringCooldown(varan, damageSources().explosion(this, getOwner()), varan.getMaxHealth() * 0.35f + 20f);
        for (var p : server.players()) {
            if (p.distanceToSqr(varan) < 160 * 160) p.displayClientMessage(Component.translatable("message.reikogodzilla.varan_swallowed"), true);
        }
        discard();
    }

    private void burst() {
        if (level() instanceof ServerLevel server) {
            server.explode(this, getX(), getY(), getZ(), 2.0f, false, Level.ExplosionInteraction.NONE);
        }
        discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Lit", isLit());
        tag.putInt("LitTicks", litTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_LIT, tag.getBoolean("Lit"));
        litTicks = tag.getInt("LitTicks");
        if (isLit()) setNoGravity(true);
    }
}

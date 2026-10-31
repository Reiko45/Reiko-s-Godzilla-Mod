package com.reiko45.godzilla.entity;

import com.reiko45.godzilla.GodzillaConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/**
 * A larva that grows while out of combat, spins a cocoon, and emerges as its adult form (Mothra, Battra).
 */
public abstract class LarvaKaijuEntity extends KaijuEntity {
    private static final EntityDataAccessor<Boolean> DATA_COCOON = SynchedEntityData.defineId(LarvaKaijuEntity.class, EntityDataSerializers.BOOLEAN);
    private static final RawAnimation COCOON = RawAnimation.begin().thenLoop("cocoon");
    public static final int COCOON_TICKS = 1200;

    private int growth;
    private int cocoonTicks;

    protected LarvaKaijuEntity(EntityType<? extends LarvaKaijuEntity> type, Level level) {
        super(type, level);
    }

    /** The adult that hatches from the cocoon. */
    protected abstract EntityType<? extends FlyingKaijuEntity> adultType();

    /** Action-bar message when the adult emerges. */
    protected abstract String emergeMessage();

    protected void onEmerge(FlyingKaijuEntity adult) {}

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_COCOON, false);
    }

    public boolean isCocoon() {
        return entityData.get(DATA_COCOON);
    }

    @Override
    public boolean canWander() {
        return !isCocoon();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // The cocoon is tough silk: most damage is absorbed.
        return super.hurt(source, isCocoon() ? amount * 0.25f : amount);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (isCocoon()) {
            setTarget(null);
            setZza(0);
            setDeltaMovement(getDeltaMovement().multiply(0, 1, 0));
            if (++cocoonTicks >= COCOON_TICKS) emerge();
            else if (cocoonTicks % 20 == 0 && level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.END_ROD, getX(), getY(0.6), getZ(), 4, getBbWidth() * 0.5, getBbHeight() * 0.4, getBbWidth() * 0.5, 0.01);
            }
            return;
        }
        if (getTarget() == null) growth++;
        if (growth >= GodzillaConfig.get(GodzillaConfig.MOTHRA_GROWTH_TICKS, 12000) && onGround()) {
            entityData.set(DATA_COCOON, true);
            playSound(SoundEvents.WOOL_PLACE, 4f, 0.5f);
        }
    }

    private void emerge() {
        if (!(level() instanceof ServerLevel server)) return;
        FlyingKaijuEntity adult = adultType().create(server);
        if (adult == null) return;
        adult.moveTo(getX(), getY() + 1, getZ(), getYRot(), 0);
        adult.finalizeSpawn(server, server.getCurrentDifficultyAt(blockPosition()), MobSpawnType.CONVERSION, null);
        if (hasCustomName()) adult.setCustomName(getCustomName());
        adult.setFlying(true);
        server.addFreshEntity(adult);
        onEmerge(adult);
        server.sendParticles(ParticleTypes.FLASH, getX(), getY(0.5), getZ(), 1, 0, 0, 0, 0);
        server.sendParticles(ParticleTypes.END_ROD, getX(), getY(0.5), getZ(), 80, getBbWidth(), getBbHeight(), getBbWidth(), 0.1);
        for (var player : server.players()) {
            if (player.distanceToSqr(this) < 128 * 128) player.displayClientMessage(Component.translatable(emergeMessage()), true);
        }
        discard();
    }

    @Override
    protected PlayState mainAnimation(AnimationState<KaijuEntity> state) {
        if (isCocoon()) return state.setAndContinue(COCOON);
        return super.mainAnimation(state);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Growth", growth);
        tag.putBoolean("Cocoon", isCocoon());
        tag.putInt("CocoonTicks", cocoonTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        growth = tag.getInt("Growth");
        entityData.set(DATA_COCOON, tag.getBoolean("Cocoon"));
        cocoonTicks = tag.getInt("CocoonTicks");
    }
}

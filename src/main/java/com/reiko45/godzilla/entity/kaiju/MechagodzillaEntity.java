package com.reiko45.godzilla.entity.kaiju;

import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Showa Mechagodzilla (1974), built from Space Titanium by the Black Hole Planet 3 aliens. Fires the rainbow Space
 * Beam from its eyes and finger missiles from its hands, and spins its head to raise an impenetrable force field.
 * <p>
 * Mechagodzilla that arrive through the invasion event come disguised as Godzilla ("Fake Godzilla") until they take
 * enough damage or start shooting, when the disguise burns away.
 * <p>
 * Mechagodzilla 2 (Terror of Mechagodzilla, 1975) is the rebuilt version, variant 1: rotating finger missiles and a
 * stronger hull, linked to its controllers through Katsura's cyborg body. If the Black Hole aliens' signal is cut
 * after it has been linked, it shuts down for good.
 */
public class MechagodzillaEntity extends KaijuEntity {
    private static final EntityDataAccessor<Boolean> DATA_DISGUISED = SynchedEntityData.defineId(MechagodzillaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SHUTDOWN = SynchedEntityData.defineId(MechagodzillaEntity.class, EntityDataSerializers.BOOLEAN);
    public static final int SHOWA = 0;
    public static final int MECHAGODZILLA_2 = 1;
    private boolean linked;

    public MechagodzillaEntity(EntityType<? extends MechagodzillaEntity> type, Level level) {
        super(type, level);
        this.xpReward = 550;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(900, 26, 12, 26, 0.21);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("space_beam", BeamStyle.SPACE_BEAM, 2, 0.35f, ModDamageTypes.SPACE_BEAM, 15, 50, 180)
                .damage(9f, 3).blocks(1.4f, false).tracking(0.18f, 0f)
                .sounds(ModSounds.SPACE_BEAM_CHARGE, ModSounds.SPACE_BEAM_FIRE)
                .chargeParticle(ParticleTypes.END_ROD)
                .range(4, 52));
        list.add(new MissileBarrageAbility(new Vec3(-2.3, 6.2, 2.4), new Vec3(2.3, 6.2, 2.4)).when(k -> k.getVariant() == SHOWA));
        list.add(new MissileBarrageAbility("rotating_missiles", new Vec3(-2.3, 6.2, 2.4), new Vec3(2.3, 6.2, 2.4),
                com.reiko45.godzilla.entity.projectile.KaijuMissileEntity.Payload.EXPLOSIVE, 140).interval(2).when(k -> k.getVariant() == MECHAGODZILLA_2));
        list.add(new ForceFieldAbility());
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 2.0f, 2.0f, new Vec3(0, 8.3, 2.0), 1.3f),
                new KaijuPart(this, "tail", 2.4f, 2.0f, new Vec3(0, 1.6, -4.2), 0.8f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(index == 0 ? -0.45 : 0.45, 9.25, 2.8);
    }

    @Override
    protected float regenPerSecond() {
        return 0f;
    }

    /** Built and controlled by the Black Hole Planet 3 aliens. */
    @Override
    public boolean controllableBy(com.reiko45.godzilla.world.AlienSignal signal) {
        return signal == com.reiko45.godzilla.world.AlienSignal.BLACK_HOLE && !isShutDown();
    }

    @Override
    protected void onControlled(com.reiko45.godzilla.world.AlienSignal signal) {
        if (getVariant() == MECHAGODZILLA_2) linked = true;
    }

    /** Katsura's link is cut: Mechagodzilla 2 goes dead. */
    @Override
    protected void onControlLost(com.reiko45.godzilla.world.AlienSignal signal) {
        if (getVariant() != MECHAGODZILLA_2 || !linked) return;
        entityData.set(DATA_SHUTDOWN, true);
        if (isAbilityActive()) stopAbility();
        playSound(SoundEvents.BEACON_DEACTIVATE, 6f, 0.4f);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY(0.7), getZ(), 60, getBbWidth() * 0.5, getBbHeight() * 0.3, getBbWidth() * 0.5, 0.02);
            for (var player : server.players()) {
                if (player.distanceToSqr(this) < 160 * 160) player.displayClientMessage(Component.translatable("message.reikogodzilla.mg2_shutdown"), true);
            }
        }
    }

    /** Mechagodzilla 2 relays the Black Hole aliens' orders to Titanosaurus. */
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (getVariant() == MECHAGODZILLA_2 && !isShutDown() && tickCount % 20 == 0 && level() instanceof ServerLevel server) {
            com.reiko45.godzilla.world.AlienControl.broadcast(server, position(), 128, com.reiko45.godzilla.world.AlienSignal.BLACK_HOLE, this);
        }
    }

    public boolean isShutDown() {
        return entityData.get(DATA_SHUTDOWN);
    }

    @Override
    public boolean isImmobile() {
        return isShutDown() || super.isImmobile();
    }

    @Override
    public boolean wantsToAttack(LivingEntity other) {
        return !isShutDown() && super.wantsToAttack(other);
    }

    @Override
    protected void applyVariantAttributes() {
        var health = getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(getVariant() == MECHAGODZILLA_2 ? 1050 : 900);
        var armor = getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR);
        if (armor != null) armor.setBaseValue(getVariant() == MECHAGODZILLA_2 ? 28 : 26);
    }

    @Override
    protected Component getTypeName() {
        if (isDisguised()) return ModEntities.GODZILLA.get().getDescription();
        return getVariant() == MECHAGODZILLA_2 ? Component.translatable("entity.reikogodzilla.mechagodzilla_2") : super.getTypeName();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_DISGUISED, false);
        builder.define(DATA_SHUTDOWN, false);
    }

    public boolean isDisguised() {
        return entityData.get(DATA_DISGUISED);
    }

    public void setDisguised(boolean disguised) {
        entityData.set(DATA_DISGUISED, disguised);
    }


    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (ForceFieldAbility.isShielding(this) && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            playSound(SoundEvents.SHIELD_BLOCK, 3f, 0.6f);
            Entity attacker = source.getEntity();
            if (attacker instanceof LivingEntity living && living.distanceToSqr(this) < Math.pow(getBbWidth() + 6, 2)) {
                Vec3 away = living.position().subtract(position()).multiply(1, 0, 1).normalize();
                living.push(away.x * 1.5, 0.4, away.z * 1.5);
            }
            return false;
        }
        boolean result = super.hurt(source, amount);
        if (result && isDisguised() && getHealth() < getMaxHealth() * 0.85f) reveal();
        return result;
    }

    @Override
    public void startAbility(int index, LivingEntity target) {
        if (isDisguised() && abilities.get(index) instanceof BeamAbility) reveal();
        super.startAbility(index, target);
    }

    /** The synthetic skin burns away, exposing the Space Titanium underneath. */
    public void reveal() {
        if (!isDisguised()) return;
        setDisguised(false);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY(0.5), getZ(), 80, getBbWidth() * 0.6, getBbHeight() * 0.4, getBbWidth() * 0.6, 0.05);
            server.sendParticles(ParticleTypes.FLAME, getX(), getY(0.5), getZ(), 60, getBbWidth() * 0.6, getBbHeight() * 0.4, getBbWidth() * 0.6, 0.05);
            playSound(SoundEvents.GENERIC_EXPLODE.value(), 5f, 0.7f);
            for (var player : server.players()) {
                if (player.distanceToSqr(this) < 160 * 160) {
                    player.displayClientMessage(Component.translatable("message.reikogodzilla.mechagodzilla_revealed"), true);
                }
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Disguised", isDisguised());
        tag.putBoolean("Linked", linked);
        tag.putBoolean("ShutDown", isShutDown());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setDisguised(tag.getBoolean("Disguised"));
        linked = tag.getBoolean("Linked");
        entityData.set(DATA_SHUTDOWN, tag.getBoolean("ShutDown"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isDisguised() ? ModSounds.GODZILLA_ROAR.get() : ModSounds.MECHAGODZILLA_ROAR.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    protected void playStepSound(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        playSound(SoundEvents.IRON_GOLEM_STEP, 2.0f, 0.5f);
    }
}

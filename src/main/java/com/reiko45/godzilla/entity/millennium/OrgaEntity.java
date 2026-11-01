package com.reiko45.godzilla.entity.millennium;

import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Orga (Godzilla 2000: Millennium, 1999): the Millennian alien mutated by Godzilla's regenerative Organizer G1. Fires
 * a plasma cannon from its shoulder, regenerates at a terrifying rate, and unhinges its jaw to swallow its enemies
 * whole and absorb them.
 * <p>
 * Destroying the shoulder cannon (hit it hard) cripples both its cannon and its healing. Swallowing a Godzilla is a
 * fatal mistake: he blasts Orga apart from the inside.
 */
public class OrgaEntity extends KaijuEntity {
    private float shoulderDamage;
    private boolean cannonDestroyed;

    public OrgaEntity(EntityType<? extends OrgaEntity> type, Level level) {
        super(type, level);
        this.xpReward = 550;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(1100, 20, 10, 30, 0.2);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("shoulder_cannon", BeamStyle.MILLENNIAN, 1, 0.6f, ModDamageTypes.PLASMA_CANNON, 25, 40, 200)
                .damage(12f, 4).blocks(2.0f, true).chargeParticle(ParticleTypes.END_ROD).range(4, 48)
                .when(k -> !((OrgaEntity) k).cannonDestroyed));
        list.add(new HoldAbility("swallow", "grab", HoldAbility.Mode.MOUTH, 100, 10, 0.6f, 0.6f, 400)
                .maxSize(1.6f).damageType(ModDamageTypes.ENERGY_DRAIN).sound(ModSounds.ORGA_ROAR)
                .onHit((k, held) -> {
                    // Godzilla answers from inside with a nuclear pulse.
                    if (held instanceof KaijuEntity g && g.isGodzilla() && k.level() instanceof ServerLevel server) {
                        server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, k.getX(), k.getY(0.7), k.getZ(), 60, k.getBbWidth() * 0.4, k.getBbHeight() * 0.3, k.getBbWidth() * 0.4, 0.2);
                        KaijuDestruction.hurtIgnoringCooldown(k, ModDamageTypes.source(server, ModDamageTypes.NUCLEAR_PULSE, g), k.getMaxHealth() * 0.09f);
                    }
                }));
        list.add(new GrabThrowAbility());
        list.add(new RoarAbility(ModSounds.ORGA_ROAR));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 2.0f, 2.0f, new Vec3(0, 7.6, 2.4), 1.3f),
                new KaijuPart(this, "shoulder", 2.2f, 2.2f, new Vec3(-2.0, 7.2, 0.2), 1.5f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(-2.0, 8.4, 0.8);
    }

    @Override
    protected double variantScale() {
        return 1.25;
    }

    @Override
    protected float regenPerSecond() {
        return cannonDestroyed ? 0.8f : 4.0f;
    }

    @Override
    public boolean hurtFromPart(KaijuPart part, DamageSource source, float amount) {
        if (part.name.equals("shoulder") && !cannonDestroyed) {
            shoulderDamage += amount * part.damageMultiplier;
            if (shoulderDamage > getMaxHealth() * 0.12f) {
                cannonDestroyed = true;
                playSound(net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(), 6f, 0.7f);
                if (level() instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.EXPLOSION, part.getX(), part.getY(), part.getZ(), 6, 1, 1, 1, 0);
                    for (var p : server.players()) {
                        if (p.distanceToSqr(this) < 160 * 160) p.displayClientMessage(Component.translatable("message.reikogodzilla.orga_cannon"), true);
                    }
                }
            }
        }
        return super.hurtFromPart(part, source, amount);
    }

    /** Whatever Orga is swallowing hits three times as hard from inside. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        LivingEntity held = HoldAbility.heldBy(this);
        if (held != null && source.getEntity() == held) amount *= 3f;
        return super.hurt(source, amount);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("ShoulderDamage", shoulderDamage);
        tag.putBoolean("CannonDestroyed", cannonDestroyed);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        shoulderDamage = tag.getFloat("ShoulderDamage");
        cannonDestroyed = tag.getBoolean("CannonDestroyed");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ORGA_ROAR.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ORGA_ROAR.get();
    }
}

package com.reiko45.godzilla.entity.millennium;

import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The Millennian UFO (Godzilla 2000: Millennium): a solar-powered craft that lay on the sea floor for 60 million years.
 * It fires a plasma cannon, binds its enemies with psychokinetically controlled cables and knocks them back with
 * shockwaves.
 * <p>
 * It soaks up half of any Godzilla's breath and studies it: the Organizer G1 in Godzilla's cells is what the
 * Millennians are after. If the UFO is destroyed after it has absorbed enough of Godzilla, the alien inside crawls out
 * and becomes Orga.
 */
public class MillennianUfoEntity extends FlyingKaijuEntity {
    private static final float G1_NEEDED = 60f;
    private float g1;

    public MillennianUfoEntity(EntityType<? extends MillennianUfoEntity> type, Level level) {
        super(type, level);
        this.xpReward = 400;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(700, 24, 10, 16, 0.2).add(Attributes.FLYING_SPEED, 0.55);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("plasma_cannon", BeamStyle.MILLENNIAN, 1, 0.5f, ModDamageTypes.PLASMA_CANNON, 20, 30, 160)
                .damage(10f, 4).blocks(1.8f, true).chargeParticle(ParticleTypes.END_ROD).range(4, 56));
        list.add(new VineGrabAbility("cable_bind"));
        list.add(new SonicWaveAbility(10f));
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 0.2, 2.0);
    }

    @Override
    public boolean alwaysFlies() {
        return true;
    }

    @Override
    public boolean canFlyNow() {
        return true;
    }

    @Override
    protected float regenPerSecond() {
        return level().isDay() && level().canSeeSky(blockPosition().above()) ? 1.5f : 0.2f;
    }

    /** Godzilla's breath: half of it is absorbed and analysed. */
    @Override
    protected float absorbBeam(DamageSource source, float amount) {
        if (source.getEntity() instanceof KaijuEntity k && k.isGodzilla()) {
            g1 += amount;
            if (level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.END_ROD, getX(), getY(0.5), getZ(), 10, getBbWidth() * 0.4, 0.4, getBbWidth() * 0.4, 0.05);
            }
            return amount * 0.5f;
        }
        return amount;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!(level() instanceof ServerLevel server)) return;
        server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(0.5), getZ(), 2, getBbWidth() * 0.4, 0.5, getBbWidth() * 0.4, 0);
        if (g1 < G1_NEEDED) return;
        OrgaEntity orga = ModEntities.ORGA.get().create(server);
        if (orga == null) return;
        int y = server.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, getBlockX(), getBlockZ());
        orga.moveTo(getX(), y, getZ(), getYRot(), 0);
        orga.finalizeSpawn(server, server.getCurrentDifficultyAt(orga.blockPosition()), MobSpawnType.EVENT, null);
        server.addFreshEntity(orga);
        for (var p : server.players()) {
            if (p.distanceToSqr(this) < 256 * 256) p.displayClientMessage(Component.translatable("message.reikogodzilla.orga_emerges"), false);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("G1", g1);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        g1 = tag.getFloat("G1");
    }

    @Override
    public void playWingSound() {}

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.UFO_HUM.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_EXPLODE.value();
    }
}

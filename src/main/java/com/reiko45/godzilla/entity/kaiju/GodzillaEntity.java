package com.reiko45.godzilla.entity.kaiju;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Godzilla.
 * <ul>
 *     <li>Showa (1954-75): ~50 m, blue atomic breath, tail swipe.</li>
 *     <li>Heisei (1984-95): ~100 m, stronger breath and the nuclear pulse.</li>
 *     <li>Red Spiral Ray (Heisei): gained for a while when a Fire Rodan dies nearby and gives Godzilla its energy.</li>
 *     <li>Burning Godzilla is its own kaiju now: {@link BurningGodzillaEntity}.</li>
 * </ul>
 */
public class GodzillaEntity extends KaijuEntity {
    public static final int SHOWA = 0;
    public static final int HEISEI = 1;

    private static final EntityDataAccessor<Boolean> DATA_SPIRAL = SynchedEntityData.defineId(GodzillaEntity.class, EntityDataSerializers.BOOLEAN);

    private int spiralTicks;

    public GodzillaEntity(EntityType<? extends GodzillaEntity> type, Level level) {
        super(type, level);
        this.xpReward = 500;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(900, 20, 10, 28, 0.24);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.TITAN;
    }

    @Override
    public boolean isGodzilla() {
        return true;
    }

    /** The Kilaaks controlled the Showa Godzilla along with the rest of Monster Island (Destroy All Monsters). */
    @Override
    public boolean controllableBy(com.reiko45.godzilla.world.AlienSignal signal) {
        return signal == com.reiko45.godzilla.world.AlienSignal.KILAAK && getVariant() == SHOWA;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("atomic_breath", BeamStyle.ATOMIC, 1, 0.7f, ModDamageTypes.ATOMIC_BREATH, 40, 50, 260)
                .damage(10f, 4).blocks(1.5f, true).sounds(ModSounds.ATOMIC_BREATH_CHARGE, ModSounds.ATOMIC_BREATH_FIRE)
                .chargeParticle(ParticleTypes.SOUL_FIRE_FLAME).nuclear()
                .range(4, 44).when(k -> k.getVariant() == SHOWA));
        list.add(new BeamAbility("atomic_breath", BeamStyle.ATOMIC, 1, 1.0f, ModDamageTypes.ATOMIC_BREATH, 50, 60, 240)
                .damage(14f, 4).blocks(2.2f, true).sounds(ModSounds.ATOMIC_BREATH_CHARGE, ModSounds.ATOMIC_BREATH_FIRE)
                .chargeParticle(ParticleTypes.SOUL_FIRE_FLAME).nuclear()
                .range(4, 44).when(k -> k.getVariant() == HEISEI && !((GodzillaEntity) k).hasRedSpiral()));
        list.add(new TailSwipeAbility());
        list.add(new RoarAbility(ModSounds.GODZILLA_ROAR));
        list.add(new NuclearPulseAbility().when(k -> k.getVariant() == HEISEI));
        list.add(new BeamAbility("red_spiral_ray", BeamStyle.SPIRAL_RED, 1, 1.1f, ModDamageTypes.RED_SPIRAL_RAY, 40, 60, 220)
                .damage(20f, 4).blocks(2.6f, true).sounds(ModSounds.ATOMIC_BREATH_CHARGE, ModSounds.ATOMIC_BREATH_FIRE)
                .chargeParticle(ParticleTypes.FLAME).nuclear()
                .range(4, 48).when(k -> ((GodzillaEntity) k).hasRedSpiral()));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 2.0f, 2.0f, new Vec3(0, 7.9, 1.1), 1.5f),
                new KaijuPart(this, "tail", 2.6f, 2.4f, new Vec3(0, 0.8, -4.4), 0.8f),
                new KaijuPart(this, "tail_tip", 1.8f, 1.6f, new Vec3(0, 0.4, -7.6), 0.6f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        // The mouths of Reiko's Heisei and Showa models.
        return getVariant() == HEISEI ? new Vec3(0, 8.5, 2.0) : new Vec3(0, 8.2, 2.0);
    }

    @Override
    protected double variantScale() {
        return getVariant() == HEISEI ? 2.0 : 1.0;
    }

    @Override
    protected double baseStepHeight() {
        return 3.0;
    }

    @Override
    protected float regenPerSecond() {
        return getVariant() == HEISEI ? 2.0f : 1.5f;
    }

    @Override
    protected boolean attacksPlayersOnSight() {
        return GodzillaConfig.get(GodzillaConfig.GODZILLA_HOSTILE, false);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SPIRAL, false);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData data) {
        setVariant(pickVariant());
        return super.finalizeSpawn(level, difficulty, spawnType, data);
    }

    /** Showa or Heisei at random. */
    protected int pickVariant() {
        return random.nextInt(2);
    }

    @Override
    protected void applyVariantAttributes() {
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(getVariant() == HEISEI ? 1024 : 800);
    }

    // ------------------------------------------------------------------ Red Spiral Ray

    public boolean hasRedSpiral() {
        return entityData.get(DATA_SPIRAL);
    }

    /** Fire Rodan's life energy flows into Godzilla: Red Spiral Ray and a big heal. */
    public void absorbRodanEnergy(int ticks) {
        if (getVariant() != HEISEI) setVariant(HEISEI);
        spiralTicks = Math.max(spiralTicks, ticks);
        entityData.set(DATA_SPIRAL, true);
        heal(getMaxHealth() * 0.3f);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.FLAME, getX(), getY(0.5), getZ(), 120, getBbWidth(), getBbHeight() * 0.4, getBbWidth(), 0.1);
        }
        announce("message.reikogodzilla.red_spiral", ChatFormatting.RED);
    }

    protected void roarNow() {
        level().playSound(null, getX(), getEyeY(), getZ(), ModSounds.GODZILLA_ROAR.get(), net.minecraft.sounds.SoundSource.HOSTILE, 10f, 0.8f);
        triggerAnim("action", "roar");
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (spiralTicks > 0 && --spiralTicks <= 0) entityData.set(DATA_SPIRAL, false);
    }

    protected void announce(String key, ChatFormatting color) {
        if (!(level() instanceof ServerLevel server)) return;
        for (var p : server.players()) {
            if (p.distanceToSqr(this) < 192 * 192) p.sendSystemMessage(Component.translatable(key).withStyle(color));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("RedSpiral", spiralTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        spiralTicks = tag.getInt("RedSpiral");
        entityData.set(DATA_SPIRAL, spiralTicks > 0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.GODZILLA_ROAR.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GODZILLA_DEATH.get();
    }
}

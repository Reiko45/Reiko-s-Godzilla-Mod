package com.reiko45.godzilla.entity.kaiju;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Godzilla. Phase 1 ships two incarnations:
 * <ul>
 *     <li>Showa (1954-75): ~50 m, blue atomic breath, tail swipe.</li>
 *     <li>Heisei (1984-95): ~100 m, stronger breath and the nuclear pulse.</li>
 * </ul>
 * Titans leave players alone unless provoked (configurable), fight invading kaiju, and regenerate quickly.
 */
public class GodzillaEntity extends KaijuEntity {
    public static final int SHOWA = 0;
    public static final int HEISEI = 1;

    public GodzillaEntity(EntityType<? extends GodzillaEntity> type, Level level) {
        super(type, level);
        this.xpReward = 500;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(900, 20, 10, 28, 0.24);
    }

    @Override
    public KaijuFaction getFaction() {
        return KaijuFaction.TITAN;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("atomic_breath_showa", BeamStyle.ATOMIC, 1, 0.7f, ModDamageTypes.ATOMIC_BREATH, 40, 50, 260)
                .damage(10f, 4).blocks(1.5f, true).sounds(ModSounds.ATOMIC_BREATH_CHARGE, ModSounds.ATOMIC_BREATH_FIRE)
                .chargeParticle(ParticleTypes.SOUL_FIRE_FLAME)
                .range(4, 44).when(k -> k.getVariant() == SHOWA));
        list.add(new BeamAbility("atomic_breath_heisei", BeamStyle.ATOMIC, 1, 1.0f, ModDamageTypes.ATOMIC_BREATH, 50, 60, 240)
                .damage(14f, 4).blocks(2.2f, true).sounds(ModSounds.ATOMIC_BREATH_CHARGE, ModSounds.ATOMIC_BREATH_FIRE)
                .chargeParticle(ParticleTypes.SOUL_FIRE_FLAME)
                .range(4, 44).when(k -> k.getVariant() == HEISEI));
        list.add(new TailSwipeAbility());
        list.add(new RoarAbility(ModSounds.GODZILLA_ROAR));
        list.add(new NuclearPulseAbility().when(k -> k.getVariant() == HEISEI));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 2.0f, 2.0f, new Vec3(0, 8.3, 2.2), 1.5f),
                new KaijuPart(this, "tail", 2.6f, 2.4f, new Vec3(0, 1.4, -4.2), 0.8f),
                new KaijuPart(this, "tail_tip", 1.8f, 1.6f, new Vec3(0, 0.6, -7.4), 0.6f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 9.0, 2.9);
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

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData data) {
        setVariant(random.nextInt(2));
        applyVariantHealth();
        return super.finalizeSpawn(level, difficulty, spawnType, data);
    }

    @Override
    public void setVariant(int variant) {
        super.setVariant(variant);
        applyVariantHealth();
    }

    private void applyVariantHealth() {
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(getVariant() == HEISEI ? 1024 : 800);
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

package com.reiko45.godzilla.entity.monsterverse;

import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.RoarAbility;
import com.reiko45.godzilla.entity.ability.SnatchAbility;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Warbats, serpent-like flyers of the Hollow Earth (Godzilla vs. Kong): one tore a HEAV out of the sky. They snatch
 * prey, carry it high and drop it, and rip machines apart.
 */
public class WarbatEntity extends FlyingKaijuEntity {
    private boolean natural;

    public WarbatEntity(EntityType<? extends WarbatEntity> type, Level level) {
        super(type, level);
        this.xpReward = 80;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(240, 6, 2, 16, 0.26).add(Attributes.FLYING_SPEED, 0.9).add(Attributes.FOLLOW_RANGE, 64);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new SnatchAbility());
        list.add(new RoarAbility(ModSounds.WARBAT_SCREECH));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty, MobSpawnType type, @Nullable SpawnGroupData data) {
        natural = type == MobSpawnType.NATURAL || type == MobSpawnType.CHUNK_GENERATION;
        return super.finalizeSpawn(level, difficulty, type, data);
    }

    @Override
    public boolean showsBossBar() {
        return false;
    }

    @Override
    public boolean isPersistenceRequired() {
        return !natural && super.isPersistenceRequired();
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return natural && !hasCustomName() && distanceToClosestPlayer > 128 * 128;
    }

    @Override
    public int landChance() {
        return 2400;
    }

    @Override
    protected float regenPerSecond() {
        return 0.1f;
    }

    @Override
    public boolean canCrushBlocks() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Natural", natural);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        natural = tag.getBoolean("Natural");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.WARBAT_SCREECH.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.WARBAT_SCREECH.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.WARBAT_SCREECH.get();
    }
}

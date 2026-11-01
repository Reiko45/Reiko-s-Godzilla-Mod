package com.reiko45.godzilla.entity.monsterverse;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.QuakeAbility;
import com.reiko45.godzilla.entity.ability.RoarAbility;
import com.reiko45.godzilla.entity.ability.RollAbility;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Methuselah, the Titan with a mountain on his back (King of the Monsters). He sleeps disguised as part of the
 * landscape and wakes when King Ghidorah's Alpha Call or the ORCA reaches him, or when someone digs into him. Then
 * he is an unstoppable, crushing weight that follows the Alpha.
 */
public class MethuselahEntity extends KaijuEntity {
    private static final EntityDataAccessor<Boolean> DATA_DORMANT = SynchedEntityData.defineId(MethuselahEntity.class, EntityDataSerializers.BOOLEAN);

    public MethuselahEntity(EntityType<? extends MethuselahEntity> type, Level level) {
        super(type, level);
        this.xpReward = 500;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(1400, 28, 14, 30, 0.16);
    }

    @Override
    public KaijuFaction getFaction() {
        return alphaAware(KaijuFaction.TITAN);
    }

    @Override
    public boolean followsAlpha() {
        return true;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new RollAbility("mountain_charge", "charge", "walk", 0.9));
        list.add(new QuakeAbility("quake_stomp", 10f, 1.4f, 260));
        list.add(new RoarAbility(ModSounds.METHUSELAH_RUMBLE));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{new KaijuPart(this, "head", 2.4f, 2.2f, new Vec3(0, 3.6, 5.0), 1.3f)};
    }

    @Override
    protected double variantScale() {
        return 2.0;
    }

    @Override
    protected double baseStepHeight() {
        return 3.0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_DORMANT, false);
    }

    public boolean isDormant() {
        return entityData.get(DATA_DORMANT);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty, MobSpawnType type, @Nullable SpawnGroupData data) {
        if (type == MobSpawnType.STRUCTURE) entityData.set(DATA_DORMANT, true);
        return super.finalizeSpawn(level, difficulty, type, data);
    }

    /** The mountain rises. */
    @Override
    public void wakeUp() {
        if (!isDormant()) return;
        entityData.set(DATA_DORMANT, false);
        triggerAnim("action", "roar");
        playSound(ModSounds.METHUSELAH_RUMBLE.get(), 10f, 0.6f);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), getX(), getY(0.6), getZ(), 300,
                    getBbWidth() * 0.6, getBbHeight() * 0.4, getBbWidth() * 0.6, 0.3);
            for (var p : server.players()) {
                if (p.distanceToSqr(this) < 192 * 192) p.sendSystemMessage(Component.translatable("message.reikogodzilla.methuselah_wakes").withStyle(ChatFormatting.GOLD));
            }
        }
    }

    @Override
    public boolean isImmobile() {
        return isDormant() || super.isImmobile();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isDormant() && !level().isClientSide && source.getEntity() != null) wakeUp();
        return super.hurt(source, isDormant() ? amount * 0.2f : amount);
    }

    @Override
    public boolean showsBossBar() {
        return !isDormant() && super.showsBossBar();
    }

    @Override
    public boolean canWander() {
        return !isDormant();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Dormant", isDormant());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_DORMANT, tag.getBoolean("Dormant"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isDormant() ? null : ModSounds.METHUSELAH_RUMBLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.METHUSELAH_RUMBLE.get();
    }
}

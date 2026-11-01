package com.reiko45.godzilla.entity.reiwa;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.*;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.registry.ModItems;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.util.KaijuDestruction;
import com.reiko45.godzilla.world.InstantBeam;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Ghidorah, the Planet Eater (GODZILLA: The Planet Eater, 2018): an extradimensional being that devoured the Exif's
 * homeworld. Only its golden necks reach into our world, through rifts in the sky, and its bites drain the life out of
 * whatever they seize. Our physics doesn't apply to it while the Exif's Garbetrium totem keeps "observing" it for this
 * world: nothing can touch it. Break the totem and it becomes mortal, and fragile.
 */
public class PlanetEaterGhidorahEntity extends FlyingKaijuEntity {
    @Nullable
    private BlockPos observer;
    private int idle;

    public PlanetEaterGhidorahEntity(EntityType<? extends PlanetEaterGhidorahEntity> type, Level level) {
        super(type, level);
        this.xpReward = 600;
        this.noPhysics = false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(320, 0, 0, 20, 0.3).add(Attributes.FLYING_SPEED, 0.9);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new HoldAbility("life_bite", "grab", HoldAbility.Mode.MOUTH, 120, 10, 0.3f, 0.05f, 140)
                .damageType(ModDamageTypes.LIFE_DRAIN).sound(ModSounds.PLANET_EATER_HUM).onHit((k, e) -> {
                    if (e instanceof KaijuEntity kaiju) {
                        kaiju.stun(15);
                        KaijuDestruction.hurtIgnoringCooldown(kaiju, ModDamageTypes.source(k.level(), ModDamageTypes.LIFE_DRAIN, k), kaiju.getMaxHealth() * 0.03f);
                    } else {
                        e.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 0));
                        KaijuDestruction.hurtIgnoringCooldown(e, ModDamageTypes.source(k.level(), ModDamageTypes.LIFE_DRAIN, k), 4f);
                    }
                    if (k.level() instanceof ServerLevel s) s.sendParticles(ParticleTypes.END_ROD, e.getX(), e.getY(0.5), e.getZ(), 8, 0.4, 0.6, 0.4, 0.05);
                }));
        list.add(new GravityTornadoAbility());
    }

    @Override
    protected double variantScale() {
        return 3.0;
    }

    public void setObserver(@Nullable BlockPos pos) {
        this.observer = pos;
    }

    public boolean isObserved() {
        return observer != null;
    }

    /** The totem was broken: Ghidorah is part of our world now, and can be hurt. */
    public void observerLost() {
        observer = null;
        if (level() instanceof ServerLevel server) server.sendParticles(ParticleTypes.FLASH, getX(), getY(0.5), getZ(), 1, 0, 0, 0, 0);
    }

    @Override
    public boolean alwaysFlies() {
        return true;
    }

    @Override
    public boolean prefersFlightInCombat() {
        return true;
    }

    @Override
    protected boolean attacksPlayersOnSight() {
        return true;
    }

    @Override
    public boolean showsBossBar() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isObserved() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (level() instanceof ServerLevel server && tickCount % 5 == 0) {
                server.sendParticles(ParticleTypes.ENCHANT, getX(), getY(0.5), getZ(), 20, getBbWidth() * 0.5, getBbHeight() * 0.5, getBbWidth() * 0.5, 0.5);
            }
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel server)) return;
        if (observer != null && tickCount % 20 == 0 && server.isLoaded(observer)
                && !(server.getBlockState(observer).getBlock() instanceof com.reiko45.godzilla.block.GarbetriumTotemBlock)) observerLost();
        if (tickCount % 4 == 0) {
            server.sendParticles(isObserved() ? ParticleTypes.END_ROD : ParticleTypes.ELECTRIC_SPARK, getX(), getY(0.5), getZ(), 3,
                    getBbWidth() * 0.4, getBbHeight() * 0.4, getBbWidth() * 0.4, 0.02);
        }
        // With no one left to eat, it withdraws through its rift.
        idle = getTarget() == null ? idle + 1 : 0;
        if (idle > 6000) {
            server.sendParticles(ParticleTypes.REVERSE_PORTAL, getX(), getY(0.5), getZ(), 80, 2, 2, 2, 0.4);
            discard();
        }
    }

    @Nullable
    @Override
    public float[] getTint() {
        return isObserved() ? new float[]{1.0f, 0.95f, 0.7f} : super.getTint();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (observer != null) tag.put("Observer", net.minecraft.nbt.NbtUtils.writeBlockPos(observer));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        observer = net.minecraft.nbt.NbtUtils.readBlockPos(tag, "Observer").orElse(null);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.PLANET_EATER_HUM.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDER_DRAGON_DEATH;
    }
}

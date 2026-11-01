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
 * The Kraken (Skull Island, 2023): a giant octopus that wants Kong's crown. It wraps prey in its tentacles, clouds
 * the water with ink and drags boats under.
 */
public class KrakenEntity extends KaijuEntity {
    public KrakenEntity(EntityType<? extends KrakenEntity> type, Level level) {
        super(type, level);
        this.xpReward = 500;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(700, 10, 6, 24, 0.24);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new HoldAbility("tentacle_wrap", "grab", HoldAbility.Mode.BODY, 100, 10, 0.5f, 0f, 220).damageType(ModDamageTypes.CONSTRICTION));
        list.add(new InkCloudAbility());
        list.add(new TailSwipeAbility());
        list.add(new RoarAbility(ModSounds.KRAKEN_GROWL));
    }

    @Override
    protected double variantScale() {
        return 2.0;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel server) || tickCount % 20 != 0 || !isInWater()) return;
        for (net.minecraft.world.entity.vehicle.Boat boat : server.getEntitiesOfClass(net.minecraft.world.entity.vehicle.Boat.class, getBoundingBox().inflate(4 + getBbWidth()))) {
            boat.ejectPassengers();
            server.sendParticles(ParticleTypes.SPLASH, boat.getX(), boat.getY(), boat.getZ(), 30, 1, 0.5, 1, 0.2);
            boat.kill();
        }
    }

    @Override
    protected float regenPerSecond() {
        return isInWater() ? 2f : 0f;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.KRAKEN_GROWL.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SQUID_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SQUID_DEATH;
    }
}

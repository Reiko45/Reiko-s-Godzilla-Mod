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
 * Servum (the anime trilogy): Godzilla Earth's subspecies. Packs of wyvern-like flyers with metal-hard flesh, and
 * burrowing worms that seize prey with root-like tentacles. Both give off an electromagnetic pulse that knocks out
 * machines. Nanometal weapons (like the Houtua's arrowheads) cut straight through them.
 */
public class ServumEntity extends FlyingKaijuEntity {
    public static final int FLYING = 0, WORM = 1;

    public ServumEntity(EntityType<? extends ServumEntity> type, Level level) {
        super(type, level);
        this.xpReward = 30;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(60, 10, 4, 8, 0.3).add(Attributes.FLYING_SPEED, 0.75);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new DiveAbility(1.8f).when(k -> k.getVariant() == FLYING));
        list.add(new EmpPulseAbility());
        list.add(new BurrowAbility("burrow", 0.35, 1.6f, 300).when(k -> k.getVariant() == WORM));
        list.add(new VineGrabAbility("root_tentacles").when(k -> k.getVariant() == WORM));
    }

    @Override
    protected double variantScale() {
        return getVariant() == WORM ? 1.2 : 0.6;
    }

    @Override
    public net.minecraft.world.entity.EntityDimensions getDefaultDimensions(net.minecraft.world.entity.Pose pose) {
        return getVariant() == WORM ? net.minecraft.world.entity.EntityDimensions.scalable(3.0f, 1.4f) : net.minecraft.world.entity.EntityDimensions.scalable(3.0f, 2.6f);
    }

    @Override
    public boolean canFlyNow() {
        return getVariant() == FLYING;
    }

    @Override
    public boolean alwaysFlies() {
        return getVariant() == FLYING;
    }

    @Override
    public boolean prefersFlightInCombat() {
        return getVariant() == FLYING;
    }

    @Override
    public Component getTypeName() {
        return Component.translatable(getVariant() == WORM ? "entity.reikogodzilla.servum_worm" : "entity.reikogodzilla.servum");
    }

    @Override
    public boolean showsBossBar() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof LivingEntity attacker && attacker.getMainHandItem().is(ModItems.NANOMETAL_BLADE.get())
                && source.getDirectEntity() == attacker) amount *= 10f;
        if (source.is(ModDamageTypes.NANOMETAL)) amount *= 10f;
        return super.hurt(source, amount);
    }

    @Override
    public void finalizeSpawnVariant(MobSpawnType type) {
        if (type == MobSpawnType.NATURAL || type == MobSpawnType.STRUCTURE || type == MobSpawnType.EVENT) setVariant(random.nextInt(3) == 0 ? WORM : FLYING);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.SERVUM_SHRIEK.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PHANTOM_DEATH;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return distance > 128 * 128;
    }
}

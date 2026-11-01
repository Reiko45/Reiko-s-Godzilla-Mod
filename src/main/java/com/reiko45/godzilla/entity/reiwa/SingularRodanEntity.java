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
 * Rodan from Godzilla Singular Point: pterosaur kaiju that pour out of the Red Dust in swarms, screaming in radio waves
 * that scramble electronics. The small ones break down if they stray from the dust for too long. A larger adult
 * leads each flock.
 */
public class SingularRodanEntity extends FlyingKaijuEntity {
    public static final int JUVENILE = 0, ADULT = 1;
    private int awayFromDust;

    public SingularRodanEntity(EntityType<? extends SingularRodanEntity> type, Level level) {
        super(type, level);
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(30, 4, 0, 6, 0.3).add(Attributes.FLYING_SPEED, 0.85);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new DiveAbility(1.8f));
        list.add(new EmpPulseAbility().when(k -> k.getVariant() == ADULT));
        list.add(new RoarAbility(ModSounds.RODAN_SP_CRY).when(k -> k.getVariant() == ADULT));
    }

    @Override
    protected double variantScale() {
        return getVariant() == ADULT ? 3.5 : 1.2;
    }

    @Override
    protected void applyVariantAttributes() {
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(getVariant() == ADULT ? 240 : 30);
        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(getVariant() == ADULT ? 18 : 6);
    }

    @Override
    public Component getTypeName() {
        return Component.translatable(getVariant() == ADULT ? "entity.reikogodzilla.rodan_sp_adult" : "entity.reikogodzilla.rodan_sp");
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
    public boolean showsBossBar() {
        return getVariant() == ADULT && super.showsBossBar();
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel server) || getVariant() == ADULT || tickCount % 200 != 0) return;
        if (com.reiko45.godzilla.block.RedDustBlock.density(server, blockPosition(), 32) > 0) awayFromDust = 0;
        else if (++awayFromDust > 30) hurt(server.damageSources().starve(), 4f);
        // Their screams jam radios and machines nearby.
        com.reiko45.godzilla.world.Emp.pulse(server, position(), 10, 40, false);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return getVariant() == JUVENILE && distance > 128 * 128;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.RODAN_SP_CRY.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PHANTOM_DEATH;
    }
}

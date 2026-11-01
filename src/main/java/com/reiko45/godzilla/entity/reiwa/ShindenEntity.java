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
 * The Kyushu J7W1 Shinden (Godzilla Minus One): a canard fighter, its guns cut down to two and its nose packed with
 * explosives. R fires the cannons, V arms (or disarms) the charge, G pulls the ejection seat. Armed and flying fast,
 * it explodes against whatever kaiju it hits; into Minus One Godzilla's head it causes an overload, and if Godzilla is
 * charging his heat ray the blast turns inward. Eject before impact: you're meant to live.
 */
public class ShindenEntity extends com.reiko45.godzilla.entity.mecha.MechaEntity {
    private static final EntityDataAccessor<Boolean> DATA_ARMED = SynchedEntityData.defineId(ShindenEntity.class, EntityDataSerializers.BOOLEAN);
    @Nullable
    private java.util.UUID ejected;
    private int ejectedTick = -1000;
    private int glide;

    public ShindenEntity(EntityType<? extends ShindenEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMechaAttributes(60, 4, 4, 0.3, 1.2);
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new MissileBarrageAbility("shinden_cannons", new Vec3(-0.3, 0.5, 1.8), new Vec3(0.3, 0.5, 1.8),
                com.reiko45.godzilla.entity.projectile.KaijuMissileEntity.Payload.RAILGUN, 30).interval(3));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ARMED, false);
    }

    public boolean isArmed() {
        return entityData.get(DATA_ARMED);
    }

    @Override
    public boolean alwaysFlies() {
        return true;
    }

    @Override
    protected double pilotFlightSpeed() {
        return 2.2;
    }

    @Override
    public Item packedItem() {
        return ModItems.SHINDEN.get();
    }

    @Override
    protected void onPilotAction(ServerPlayer pilot, int action) {
        switch (action) {
            case ACTION_PRIMARY -> tryPilotAbility(0, pilot);
            case ACTION_SECONDARY -> {
                entityData.set(DATA_ARMED, !isArmed());
                pilot.displayClientMessage(Component.translatable(isArmed() ? "message.reikogodzilla.shinden_armed" : "message.reikogodzilla.shinden_safe"), true);
                playSound(SoundEvents.LEVER_CLICK, 2f, isArmed() ? 0.6f : 1.0f);
            }
            case ACTION_TRANSFORM -> eject(pilot);
            default -> {}
        }
    }

    private void eject(ServerPlayer pilot) {
        ejected = pilot.getUUID();
        ejectedTick = tickCount;
        glide = 100;
        pilot.stopRiding();
        pilot.setDeltaMovement(getDeltaMovement().add(0, 1.6, 0));
        pilot.hurtMarked = true;
        pilot.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 400, 0));
        playSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, 3f, 0.8f);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel server)) return;
        // After the pilot ejects, the plane holds its course.
        if (glide > 0 && getPilot() == null) {
            glide--;
            Vec3 forward = Vec3.directionFromRotation(getXRot(), getYRot());
            setDeltaMovement(forward.scale(1.6));
            hurtMarked = true;
        }
        if (!isArmed() || getDeltaMovement().length() < 0.6) return;
        AABB nose = getBoundingBox().inflate(1.2);
        for (Entity e : server.getEntities(this, nose, e -> e instanceof KaijuEntity || e instanceof KaijuPart)) {
            KaijuEntity kaiju = e instanceof KaijuPart p ? p.getParent() : (KaijuEntity) e;
            if (kaiju == this || kaiju.getFaction() == KaijuFaction.HUMAN) continue;
            impact(server, kaiju, e instanceof KaijuPart p ? p.name : null);
            return;
        }
    }

    private void impact(ServerLevel server, KaijuEntity kaiju, @Nullable String part) {
        Player pilot = getPilot();
        Entity cause = pilot != null ? pilot : ejected != null ? server.getEntity(ejected) : this;
        if (kaiju instanceof MinusOneGodzillaEntity godzilla && ("head".equals(part) || godzilla.getBeamOrigin(0, 1f).distanceTo(position()) < 6)) {
            godzilla.overload(cause);
        } else {
            kaiju.hurt(ModDamageTypes.source(server, ModDamageTypes.OVERLOAD, cause), kaiju.fractionOfDesignHealth(0.12f));
        }
        server.explode(this, getX(), getY(), getZ(), 4f, Level.ExplosionInteraction.NONE);
        if (pilot != null) {
            pilot.stopRiding();
            pilot.hurt(ModDamageTypes.source(server, ModDamageTypes.OVERLOAD, this), 40f);
        } else if (ejected != null && tickCount - ejectedTick < 200 && server.getEntity(ejected) instanceof ServerPlayer p) {
            p.sendSystemMessage(Component.translatable("message.reikogodzilla.choose_to_live").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        }
        discard();
    }

    @Override
    public void playWingSound() {
        playSound(ModSounds.SHINDEN_ENGINE.get(), 0.6f, 1.0f);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Armed", isArmed());
        tag.putInt("Glide", glide);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_ARMED, tag.getBoolean("Armed"));
        glide = tag.getInt("Glide");
    }
}

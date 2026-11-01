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
 * Jet Jaguar from Godzilla Singular Point, built by the Otaki Factory. It starts as a human-sized prototype; feed it
 * an Archetype crystal to rebuild it as the larger beta, then four more to make Jet Jaguar PP, a giant. R: a jet-boosted
 * punch. V: an Archetype shield. G: if you carry the finished Diagonalizer Code, Jet Jaguar PP releases it, ending
 * every Singular Point Godzilla nearby and crystallizing the Red Dust, and is lost doing it.
 */
public class JetJaguarPPEntity extends com.reiko45.godzilla.entity.mecha.MechaEntity {
    public static final int PROTOTYPE = 0, BETA = 1, PP = 2;

    public JetJaguarPPEntity(EntityType<? extends JetJaguarPPEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMechaAttributes(120, 10, 10, 0.3, 0.6);
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new RollAbility("jet_punch", "charge", "attack", 2.2));
        list.add(new ForceFieldAbility());
        list.add(new LeapAbility("jet_leap", 1.4, 1.5f, 200));
    }

    @Override
    protected double variantScale() {
        return switch (getVariant()) {
            case PROTOTYPE -> 0.3;
            case BETA -> 0.5;
            default -> 2.0;
        };
    }

    @Override
    protected void applyVariantAttributes() {
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(switch (getVariant()) {
            case PROTOTYPE -> 120;
            case BETA -> 300;
            default -> 900;
        });
        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(getVariant() == PP ? 30 : getVariant() == BETA ? 16 : 10);
    }

    @Override
    public Component getTypeName() {
        return Component.translatable(switch (getVariant()) {
            case PROTOTYPE -> "entity.reikogodzilla.jet_jaguar_sp";
            case BETA -> "entity.reikogodzilla.jet_jaguar_beta";
            default -> "entity.reikogodzilla.jet_jaguar_pp";
        });
    }

    @Override
    public Item packedItem() {
        return ModItems.JET_JAGUAR_PP.get();
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(ModItems.ARCHETYPE_CRYSTAL.get()) && getVariant() < PP) {
            int cost = getVariant() == PROTOTYPE ? 1 : 4;
            if (stack.getCount() < cost && !player.getAbilities().instabuild) {
                if (!level().isClientSide) player.displayClientMessage(Component.translatable("message.reikogodzilla.jet_jaguar_needs", cost), true);
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
            if (!level().isClientSide) {
                stack.consume(cost, player);
                setVariant(getVariant() + 1);
                refreshDimensions();
                setHealth(getMaxHealth());
                playSound(SoundEvents.IRON_GOLEM_REPAIR, 3f, 0.6f);
                if (level() instanceof ServerLevel server) server.sendParticles(ParticleTypes.END_ROD, getX(), getY(0.5), getZ(), 60, getBbWidth(), getBbHeight() * 0.5, getBbWidth(), 0.1);
                player.displayClientMessage(Component.translatable(getVariant() == PP ? "message.reikogodzilla.jet_jaguar_pp" : "message.reikogodzilla.jet_jaguar_beta"), true);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (player.isSecondaryUseActive() && !isVehicle() && getVariant() > PROTOTYPE) {
            if (!level().isClientSide) player.displayClientMessage(Component.translatable("message.reikogodzilla.too_big_to_pack"), true);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void onPilotAction(ServerPlayer pilot, int action) {
        switch (action) {
            case ACTION_PRIMARY -> tryPilotAbility(0, pilot);
            case ACTION_SECONDARY -> tryPilotAbility(1, pilot);
            case ACTION_TRANSFORM -> {
                if (getVariant() == PP && pilot.getInventory().contains(new ItemStack(ModItems.DIAGONALIZER_CODE.get()))) releaseCode(pilot);
                else tryPilotAbility(2, pilot);
            }
            default -> {}
        }
    }

    /** Jet Jaguar PP's last act. */
    private void releaseCode(ServerPlayer pilot) {
        if (!(level() instanceof ServerLevel server)) return;
        int slot = pilot.getInventory().findSlotMatchingItem(new ItemStack(ModItems.DIAGONALIZER_CODE.get()));
        if (slot >= 0) pilot.getInventory().removeItem(slot, 1);
        var godzillas = server.getEntitiesOfClass(SingularGodzillaEntity.class, getBoundingBox().inflate(128));
        godzillas.forEach(SingularGodzillaEntity::diagonalize);
        com.reiko45.godzilla.block.RedDustBlock.crystallize(server, blockPosition(), 48);
        server.sendParticles(ParticleTypes.END_ROD, getX(), getY(0.5), getZ(), 300, 16, 12, 16, 0.3);
        pilot.stopRiding();
        pilot.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 300, 0));
        for (ServerPlayer p : server.players()) {
            if (p.distanceToSqr(this) < 256 * 256) p.sendSystemMessage(Component.translatable(godzillas.isEmpty()
                    ? "message.reikogodzilla.code_released_empty" : "message.reikogodzilla.code_released").withStyle(ChatFormatting.AQUA));
        }
        hurt(server.damageSources().genericKill(), Float.MAX_VALUE);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }
}

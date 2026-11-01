package com.reiko45.godzilla.entity.monsterverse;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.mecha.MechaEntity;
import com.reiko45.godzilla.registry.ModItems;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.List;

/**
 * Hollow Earth Aerial Vehicle (Godzilla vs. Kong). Its reverse-gravity drives are the only safe way through a vortex
 * into the Hollow Earth: fly into one and you come out the other side unharmed. Unarmed.
 * R: Monarch sonar scan (marks every Titan nearby). V: decoy flares (shakes off anything chasing it).
 */
public class HeavEntity extends MechaEntity {
    private int scanCooldown;
    private int flareCooldown;

    public HeavEntity(EntityType<? extends HeavEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMechaAttributes(160, 10, 4, 0.3, 0.95);
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {}

    @Override
    public Item packedItem() {
        return ModItems.HEAV.get();
    }

    @Override
    public boolean alwaysFlies() {
        return true;
    }

    @Override
    protected double pilotFlightSpeed() {
        return 1.3;
    }

    @Override
    public boolean wantsToAttack(LivingEntity other) {
        return false;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (scanCooldown > 0) scanCooldown--;
        if (flareCooldown > 0) flareCooldown--;
    }

    @Override
    protected void onPilotAction(ServerPlayer pilot, int action) {
        if (!(level() instanceof ServerLevel server)) return;
        if (isEmpDisabled()) {
            pilot.displayClientMessage(Component.translatable("message.reikogodzilla.emp_offline").withStyle(ChatFormatting.RED), true);
            return;
        }
        if (action == ACTION_PRIMARY) {
            if (scanCooldown > 0) {
                pilot.displayClientMessage(Component.translatable("message.reikogodzilla.recharging", Component.translatable("ability.reikogodzilla.sonar_scan")), true);
                return;
            }
            scanCooldown = 200;
            List<KaijuEntity> titans = server.getEntitiesOfClass(KaijuEntity.class, getBoundingBox().inflate(192),
                    k -> k.isAlive() && k != this && !(k instanceof MechaEntity));
            titans.sort(Comparator.comparingDouble(this::distanceToSqr));
            for (KaijuEntity k : titans) k.addEffect(new MobEffectInstance(MobEffects.GLOWING, 600, 0, false, false));
            server.playSound(null, getX(), getY(), getZ(), ModSounds.ORCA_SIGNAL.get(), net.minecraft.sounds.SoundSource.PLAYERS, 2f, 1.4f);
            if (titans.isEmpty()) {
                pilot.displayClientMessage(Component.translatable("message.reikogodzilla.sonar_none"), true);
            } else {
                KaijuEntity nearest = titans.get(0);
                pilot.displayClientMessage(Component.translatable("message.reikogodzilla.sonar_found", titans.size(), nearest.getDisplayName(),
                        (int) Math.sqrt(distanceToSqr(nearest))).withStyle(ChatFormatting.AQUA), true);
            }
        } else if (action == ACTION_SECONDARY) {
            if (flareCooldown > 0) {
                pilot.displayClientMessage(Component.translatable("message.reikogodzilla.recharging", Component.translatable("ability.reikogodzilla.flares")), true);
                return;
            }
            flareCooldown = 300;
            server.sendParticles(ParticleTypes.FIREWORK, getX(), getY(), getZ(), 60, 3, 1, 3, 0.2);
            server.sendParticles(ParticleTypes.FLAME, getX(), getY(), getZ(), 30, 3, 1, 3, 0.05);
            playSound(SoundEvents.FIREWORK_ROCKET_BLAST, 3f, 0.8f);
            for (KaijuEntity k : server.getEntitiesOfClass(KaijuEntity.class, getBoundingBox().inflate(64), k -> k.getTarget() == this || k.getTarget() == pilot)) {
                k.setTarget(null);
                if (k.isAbilityActive()) k.stopAbility();
            }
        }
    }
}

package com.reiko45.godzilla.entity.kaiju;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.entity.projectile.AcidSapEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

/**
 * Biollante (1989): rose cells, human cells and G-Cells grown into a giant plant kaiju. She never moves, but her vines
 * reach far and she regenerates fast.
 * <ul>
 *     <li>Flower Beast: lashes and grabs with vines, releases healing/poison spores.</li>
 *     <li>Plant Beast: when the Flower Beast is "killed" her spores rise and she returns in her monstrous final form,
 *     spraying radioactive acid sap.</li>
 * </ul>
 * Grown from a G-Cell Rose planted near water. She burns easily.
 */
public class BiollanteEntity extends KaijuEntity {
    public static final int FLOWER = 0;
    public static final int PLANT_BEAST = 1;

    public BiollanteEntity(EntityType<? extends BiollanteEntity> type, Level level) {
        super(type, level);
        this.xpReward = 600;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(700, 10, 6, 18, 0.0)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    public KaijuFaction getFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new VineLashAbility());
        list.add(new VineGrabAbility());
        list.add(new SporeBurstAbility().when(k -> k.getVariant() == FLOWER));
        list.add(new SprayAbility("acid_sap", k -> new AcidSapEntity(k.level(), k), () -> SoundEvents.SLIME_ATTACK, 1.5f, 8f, 40, 160)
                .interval(3).range(4, 36).when(k -> k.getVariant() == PLANT_BEAST));
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return getVariant() == PLANT_BEAST ? new Vec3(0, 13.5, 3.0) : new Vec3(0, 12.0, 0.5);
    }

    @Override
    protected double variantScale() {
        return getVariant() == PLANT_BEAST ? 1.15 : 0.75;
    }

    @Override
    protected float regenPerSecond() {
        return getVariant() == PLANT_BEAST ? 4f : 3f;
    }

    @Override
    public boolean canWander() {
        return false;
    }

    @Override
    public double getMeleeReach(LivingEntity target) {
        return 12 * getScale() + target.getBbWidth();
    }

    @Override
    protected boolean attacksPlayersOnSight() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // A plant: fire hurts her badly.
        if (source.is(DamageTypeTags.IS_FIRE) || source.is(com.reiko45.godzilla.registry.ModDamageTypes.ATOMIC_BREATH)) amount *= 1.4f;
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        if (getVariant() == FLOWER && !level().isClientSide) {
            // Her spores scatter and she returns as the Plant Beast.
            setVariant(PLANT_BEAST);
            var health = getAttribute(Attributes.MAX_HEALTH);
            if (health != null) health.setBaseValue(1000);
            setHealth(getMaxHealth());
            if (level() instanceof ServerLevel server) {
                server.sendParticles(new DustParticleOptions(new Vector3f(1f, 0.85f, 0.3f), 2f), getX(), getY(0.6), getZ(), 200,
                        getBbWidth(), getBbHeight() * 0.5, getBbWidth(), 0.2);
                for (var p : server.players()) {
                    if (p.distanceToSqr(this) < 160 * 160) {
                        p.sendSystemMessage(Component.translatable("message.reikogodzilla.biollante_plant_beast").withStyle(ChatFormatting.DARK_GREEN));
                    }
                }
            }
            playSound(SoundEvents.RAVAGER_ROAR, 8f, 0.5f);
            return;
        }
        if (level() instanceof ServerLevel server) {
            // Glowing spores drift up into the sky.
            server.sendParticles(ParticleTypes.END_ROD, getX(), getY(0.5), getZ(), 300, getBbWidth(), getBbHeight() * 0.5, getBbWidth(), 0.08);
        }
        super.die(source);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        setDeltaMovement(0, getDeltaMovement().y, 0);
        if (level().isClientSide && random.nextInt(4) == 0) {
            level().addParticle(ParticleTypes.SPORE_BLOSSOM_AIR, getRandomX(1.0), getRandomY(), getRandomZ(1.0), 0, 0.02, 0);
        }
    }

    @Override
    public boolean showsBossBar() {
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.RAVAGER_ROAR;
    }

    @Override
    public float getVoicePitch() {
        return 0.6f;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GRASS_BREAK;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }
}

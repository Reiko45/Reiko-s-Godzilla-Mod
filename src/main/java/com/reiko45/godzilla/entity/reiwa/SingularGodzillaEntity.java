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
 * Godzilla from Godzilla Singular Point (2021). It evolves through four forms, shedding Red Dust (the Archetype) that
 * spreads over the land:
 * <ul>
 *     <li><b>Aquatilis:</b> a red-orange sea creature. Out of the water for long, it grows legs.</li>
 *     <li><b>Amphibia:</b> crawls ashore wrapped in Red Dust, then wraps itself in a cocoon.</li>
 *     <li><b>Terrestris:</b> a stunted upright form with a glowing blue halo. Under heavy fire it becomes...</li>
 *     <li><b>Ultima:</b> black and towering, with a thin breath that cuts through anything and a ring of breath fired
 *     all around. It keeps growing as long as Red Dust surrounds it.</li>
 * </ul>
 * The Orthogonal Diagonalizer weakens it; Jet Jaguar PP carrying the finished code can end the Catastrophe.
 */
public class SingularGodzillaEntity extends KaijuEntity {
    public static final int AQUATILIS = 0, AMPHIBIA = 1, TERRESTRIS = 2, ULTIMA = 3;
    private static final EntityDimensions[] DIMENSIONS = {EntityDimensions.scalable(4.0f, 2.2f), EntityDimensions.scalable(4.0f, 3.6f),
            EntityDimensions.scalable(2.6f, 7.0f), EntityDimensions.scalable(3.6f, 11.0f)};
    private static final EntityDataAccessor<Float> DATA_GROWTH = SynchedEntityData.defineId(SingularGodzillaEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_COCOON = SynchedEntityData.defineId(SingularGodzillaEntity.class, EntityDataSerializers.INT);
    private static final RawAnimation COCOON = RawAnimation.begin().thenLoop("dormant");
    private int stageTicks;
    private float recentDamage;

    public SingularGodzillaEntity(EntityType<? extends SingularGodzillaEntity> type, Level level) {
        super(type, level);
        this.xpReward = 1000;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(260, 12, 6, 16, 0.26);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    public boolean isGodzilla() {
        return true;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new RollAbility("swim_charge", "charge", "swim", 2.0).when(k -> k.getVariant() == AQUATILIS && k.isInWater()));
        list.add(new BeamAbility("halo_breath", BeamStyle.ATOMIC, 1, 0.35f, ModDamageTypes.ULTIMA_BREATH, 30, 30, 300)
                .damage(6f, 4).blocks(0.6f, false).range(4, 28).when(k -> k.getVariant() == TERRESTRIS && !((SingularGodzillaEntity) k).isCocooned()));
        list.add(new BeamAbility("atomic_breath", BeamStyle.ULTIMA, 1, 0.4f, ModDamageTypes.ULTIMA_BREATH, 40, 50, 260)
                .damage(24f, 3).blocks(1.0f, false).tracking(0.25f, 0f).sounds(ModSounds.SP_GODZILLA_ROAR, ModSounds.ATOMIC_BREATH_FIRE)
                .chargeParticle(ParticleTypes.CRIMSON_SPORE).nuclear().range(6, 80).when(k -> k.getVariant() == ULTIMA));
        list.add(new NuclearPulseAbility().when(k -> k.getVariant() == ULTIMA));
        list.add(new TailSwipeAbility().when(k -> k.getVariant() >= AMPHIBIA && !((SingularGodzillaEntity) k).isCocooned()));
        list.add(new RoarAbility(ModSounds.SP_GODZILLA_ROAR).when(k -> !((SingularGodzillaEntity) k).isCocooned()));
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return getVariant() == ULTIMA ? new Vec3(0, 9.6, 2.0) : new Vec3(0, 6.2, 1.6);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return DIMENSIONS[Mth.clamp(getVariant(), 0, 3)];
    }

    @Override
    protected double variantScale() {
        return switch (getVariant()) {
            case AQUATILIS -> 1.6;
            case AMPHIBIA -> 2.0;
            case TERRESTRIS -> 1.0;
            default -> 1.0 * entityData.get(DATA_GROWTH);
        };
    }

    @Override
    protected void applyVariantAttributes() {
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(switch (getVariant()) {
            case AQUATILIS -> 260;
            case AMPHIBIA -> 420;
            case TERRESTRIS -> 600;
            default -> 1100 * entityData.get(DATA_GROWTH);
        });
        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(getVariant() == ULTIMA ? 32 : 16 + getVariant() * 4);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_GROWTH, 1.0f);
        builder.define(DATA_COCOON, 0);
    }

    public boolean isCocooned() {
        return entityData.get(DATA_COCOON) > 0;
    }

    @Override
    public Component getTypeName() {
        if (isCocooned()) return Component.translatable("entity.reikogodzilla.sp_godzilla_cocoon");
        return Component.translatable(switch (getVariant()) {
            case AQUATILIS -> "entity.reikogodzilla.godzilla_aquatilis";
            case AMPHIBIA -> "entity.reikogodzilla.godzilla_amphibia";
            case TERRESTRIS -> "entity.reikogodzilla.godzilla_terrestris";
            default -> "entity.reikogodzilla.godzilla_ultima";
        });
    }

    private void evolve(int to, ServerLevel server, String message) {
        setVariant(to);
        stageTicks = 0;
        recentDamage = 0;
        refreshDimensions();
        setHealth(getMaxHealth());
        triggerAnim("action", "roar");
        playSound(ModSounds.SP_GODZILLA_ROAR.get(), 10f, 1.2f - to * 0.15f);
        server.sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.85f, 0.15f, 0.1f), 3f),
                getX(), getY(0.5), getZ(), 80, getBbWidth(), getBbHeight() * 0.5, getBbWidth(), 0);
        for (ServerPlayer p : server.players()) {
            if (p.distanceToSqr(this) < 256 * 256) p.sendSystemMessage(Component.translatable(message).withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel server)) return;
        stageTicks++;
        if (tickCount % 20 == 0) recentDamage = Math.max(0, recentDamage - getMaxHealth() * 0.0125f);
        if (isCocooned()) return;
        switch (getVariant()) {
            case AQUATILIS -> {
                if (isInWater()) stageTicks = 0;
                else if (stageTicks > 200) evolve(AMPHIBIA, server, "message.reikogodzilla.sp_amphibia");
            }
            case AMPHIBIA -> {
                if (stageTicks > 3600) {
                    entityData.set(DATA_COCOON, 2400);
                    if (isAbilityActive()) stopAbility();
                }
            }
            case TERRESTRIS -> {
                if (recentDamage > getMaxHealth() * 0.25f) evolve(ULTIMA, server, "message.reikogodzilla.sp_ultima");
            }
            case ULTIMA -> {
                double max = GodzillaConfig.get(GodzillaConfig.ULTIMA_MAX_GROWTH, 2.0);
                if (tickCount % 1200 == 0 && entityData.get(DATA_GROWTH) < max
                        && com.reiko45.godzilla.block.RedDustBlock.density(server, blockPosition(), 24) >= 6) {
                    entityData.set(DATA_GROWTH, (float) Math.min(max, entityData.get(DATA_GROWTH) + 0.1f));
                    applyVariantAttributes();
                    applyScale();
                    refreshDimensions();
                }
            }
            default -> {}
        }
        // Shedding the Archetype.
        if (getVariant() >= AMPHIBIA && tickCount % 40 == 0 && KaijuDestruction.canGrief(server, this)) {
            com.reiko45.godzilla.block.RedDustBlock.shed(server, blockPosition(), (int) (4 + getBbWidth()), getVariant() == ULTIMA ? 10 : 5);
        }
    }

    /** The cocoon ticks here: Minecraft skips the AI step while a mob is immobile. */
    @Override
    public void aiStep() {
        int cocoon = entityData.get(DATA_COCOON);
        if (cocoon > 0 && isAlive() && level() instanceof ServerLevel server) {
            entityData.set(DATA_COCOON, cocoon - 1);
            getNavigation().stop();
            setTarget(null);
            if (cocoon % 10 == 0) server.sendParticles(ParticleTypes.ASH, getX(), getY(0.5), getZ(), 10, getBbWidth() * 0.5, getBbHeight() * 0.4, getBbWidth() * 0.5, 0);
            if (cocoon == 1) evolve(TERRESTRIS, server, "message.reikogodzilla.sp_terrestris");
        }
        super.aiStep();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isCocooned()) amount *= 0.5f;
        boolean hurt = super.hurt(source, amount);
        if (hurt) recentDamage += amount;
        return hurt;
    }

    /** Hit by an Orthogonal Diagonalizer pulse: the Archetype holding it together is disrupted. */
    public void diagonalizerHit(@Nullable Entity by) {
        if (!(level() instanceof ServerLevel server)) return;
        stun(200);
        hurt(ModDamageTypes.source(server, ModDamageTypes.ARCHETYPE, by), fractionOfDesignHealth(0.25f));
    }

    /** The finished code, released by Jet Jaguar PP: Godzilla crystallizes and is gone. */
    public void diagonalize() {
        if (!(level() instanceof ServerLevel server)) return;
        com.reiko45.godzilla.block.RedDustBlock.crystallize(server, blockPosition(), 24);
        server.sendParticles(ParticleTypes.END_ROD, getX(), getY(0.5), getZ(), 200, getBbWidth(), getBbHeight() * 0.5, getBbWidth(), 0.2);
        hurt(server.damageSources().genericKill(), Float.MAX_VALUE);
    }

    @Override
    public boolean isImmobile() {
        return isCocooned() || super.isImmobile();
    }

    @Override
    public boolean canWander() {
        return !isCocooned();
    }

    @Override
    protected PlayState mainAnimation(AnimationState<KaijuEntity> state) {
        if (isCocooned()) return state.setAndContinue(COCOON);
        return super.mainAnimation(state);
    }

    @Override
    public void finalizeSpawnVariant(MobSpawnType type) {
        if (type == MobSpawnType.EVENT) setVariant(AQUATILIS);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Growth", entityData.get(DATA_GROWTH));
        tag.putInt("Cocoon", entityData.get(DATA_COCOON));
        tag.putInt("StageTicks", stageTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Growth")) entityData.set(DATA_GROWTH, Math.max(1f, tag.getFloat("Growth")));
        entityData.set(DATA_COCOON, tag.getInt("Cocoon"));
        stageTicks = tag.getInt("StageTicks");
        super.readAdditionalSaveData(tag);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isCocooned() ? null : ModSounds.SP_GODZILLA_ROAR.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SP_GODZILLA_ROAR.get();
    }
}

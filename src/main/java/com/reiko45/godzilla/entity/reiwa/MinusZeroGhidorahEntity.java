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
 * Ghidorah from Godzilla Minus Zero (2026). It first appears as a smaller, single-headed creature riding a storm,
 * whipping up hurricane winds and firing lightning wrapped in anti-gravity that lifts buildings and trains into the
 * air and smashes them down. Wounded badly, or after it has survived a full day, it hides in its storm and grows two
 * more heads: King Ghidorah. It goes after Godzilla before anything else, and it follows storms (see the storm
 * beacon).
 */
public class MinusZeroGhidorahEntity extends FlyingKaijuEntity {
    public static final int SINGLE = 0, KING = 1;
    private static final EntityDataAccessor<Integer> DATA_TRANSFORM = SynchedEntityData.defineId(MinusZeroGhidorahEntity.class, EntityDataSerializers.INT);
    private int age;

    public MinusZeroGhidorahEntity(EntityType<? extends MinusZeroGhidorahEntity> type, Level level) {
        super(type, level);
        this.xpReward = 900;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(800, 16, 10, 26, 0.24).add(Attributes.FLYING_SPEED, 0.8);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(gravityBeam(1, false).when(k -> k.getVariant() == SINGLE && !((MinusZeroGhidorahEntity) k).isTransforming()));
        list.add(gravityBeam(3, true).when(k -> k.getVariant() == KING && !((MinusZeroGhidorahEntity) k).isTransforming()));
        list.add(new WindGustAbility(2.6f).when(k -> !((MinusZeroGhidorahEntity) k).isTransforming()));
        list.add(new SnatchAbility().when(k -> k.getVariant() == KING));
        list.add(new LightningStrikeAbility("storm_call", 4, 6f, 360).when(k -> k.level().isThundering()));
        list.add(new RoarAbility(ModSounds.MINUS_ZERO_GHIDORAH_ROAR));
    }

    private static KaijuAbility gravityBeam(int heads, boolean multi) {
        BeamAbility beam = new BeamAbility("gravity_beam", BeamStyle.GRAVITY, heads, 0.4f, ModDamageTypes.GRAVITY_LIFT, 30, 50, heads == 1 ? 900 : 700)
                .damage(9f, 4).blocks(0.8f, false).tracking(0.15f, 0.05f).sounds(ModSounds.GRAVITY_BEAM_CHARGE, ModSounds.GRAVITY_BEAM_FIRE)
                .chargeParticle(ParticleTypes.ELECTRIC_SPARK)
                .terrain((level, k, hit, radius) -> {
                    if (k.tickCount % 20 == 0) com.reiko45.godzilla.world.GravityField.lift(level, k, hit, 4, 60);
                });
        if (multi) beam.multiTarget();
        return beam.range(6, 56);
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 1.6f, 1.6f, new Vec3(0, 9.2, 2.6), 1.4f),
                new KaijuPart(this, "tail", 1.6f, 1.6f, new Vec3(0, 3.0, -6.0), 0.7f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        if (getVariant() == SINGLE) return new Vec3(0, 9.4, 3.0);
        return new Vec3((index - 1) * 2.4, 9.4 + (index == 1 ? 0.6 : 0), 3.0);
    }

    @Override
    protected double variantScale() {
        return getVariant() == KING ? 1.35 : 1.0;
    }

    @Override
    protected void applyVariantAttributes() {
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(getVariant() == KING ? 1300 : 800);
        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(getVariant() == KING ? 34 : 26);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TRANSFORM, 0);
    }

    public boolean isTransforming() {
        return entityData.get(DATA_TRANSFORM) > 0;
    }

    @Override
    public Component getTypeName() {
        return Component.translatable(getVariant() == KING ? "entity.reikogodzilla.king_ghidorah_minus_zero" : "entity.reikogodzilla.ghidorah_minus_zero");
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel server)) return;
        age++;
        int t = entityData.get(DATA_TRANSFORM);
        if (t > 0) {
            entityData.set(DATA_TRANSFORM, t - 1);
            setFlying(true);
            setDeltaMovement(getDeltaMovement().scale(0.5).add(0, 0.02, 0));
            if (t % 10 == 0) {
                var bolt = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(server);
                if (bolt != null) {
                    bolt.moveTo(getX() + (random.nextDouble() - 0.5) * 24, getY() - 4, getZ() + (random.nextDouble() - 0.5) * 24);
                    bolt.setVisualOnly(true);
                    server.addFreshEntity(bolt);
                }
            }
            server.sendParticles(ParticleTypes.CLOUD, getX(), getY(0.5), getZ(), 20, getBbWidth(), getBbHeight() * 0.6, getBbWidth(), 0.05);
            if (t == 1) {
                setVariant(KING);
                refreshDimensions();
                setHealth(getMaxHealth());
                triggerAnim("action", "roar");
                playSound(ModSounds.MINUS_ZERO_GHIDORAH_ROAR.get(), 14f, 0.7f);
                for (ServerPlayer p : server.players()) {
                    if (p.distanceToSqr(this) < 256 * 256) p.sendSystemMessage(Component.translatable("message.reikogodzilla.king_ghidorah_born").withStyle(ChatFormatting.GOLD));
                }
            }
            return;
        }
        if (getVariant() == SINGLE && isAlive() && (getHealth() < getMaxHealth() * 0.5f || age > 24000)) {
            entityData.set(DATA_TRANSFORM, 100);
            if (isAbilityActive()) stopAbility();
            server.setWeatherParameters(0, 6000, true, true);
            return;
        }
        // Godzilla comes first.
        if (tickCount % 40 == 0) {
            var godzillas = server.getEntitiesOfClass(MinusOneGodzillaEntity.class, getBoundingBox().inflate(128), g -> g.isAlive() && g.getVariant() != MinusOneGodzillaEntity.ODO);
            if (!godzillas.isEmpty() && !(getTarget() instanceof MinusOneGodzillaEntity)) setTarget(godzillas.get(0));
        }
        // It rides the storm: hurricane winds tear at the ground below it.
        if (isFlying() && level().isThundering() && tickCount % 20 == 0) {
            for (LivingEntity e : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(16, 24, 16), e -> e != this && !(e instanceof KaijuEntity))) {
                if (e instanceof Player p && (p.isCreative() || p.isSpectator())) continue;
                Vec3 away = e.position().subtract(position()).multiply(1, 0, 1).normalize();
                e.push(away.x * 0.8, 0.3, away.z * 0.8);
                e.hurtMarked = true;
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isTransforming() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        return super.hurt(source, amount);
    }

    @Override
    protected boolean attacksPlayersOnSight() {
        return true;
    }

    @Override
    public boolean prefersFlightInCombat() {
        return true;
    }

    @Override
    public int landChance() {
        return 600;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Age", age);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        age = tag.getInt("Age");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.MINUS_ZERO_GHIDORAH_ROAR.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MINUS_ZERO_GHIDORAH_ROAR.get();
    }
}

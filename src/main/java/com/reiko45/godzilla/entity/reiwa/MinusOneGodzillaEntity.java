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
 * Godzilla from Godzilla Minus One (2023) and Godzilla Minus Zero (2026).
 * <ul>
 *     <li><b>Odo Island form:</b> the 15 m creature of the island legend. Near a nuclear warhead it absorbs the
 *     radiation and mutates into the 50 m irradiated Godzilla (the Bikini Atoll tests).</li>
 *     <li><b>Regeneration:</b> it heals almost as fast as it's hurt, as long as nothing has hit its head for a few
 *     seconds. Big wounds leave scars that slow the healing a little. Damage from the inside (explosives in the mouth,
 *     decompression, an overload) stops the healing for a long time.</li>
 *     <li><b>Heat ray:</b> its dorsal plates ratchet up from the tail to the head in three clicks, lock and glow, then
 *     it fires. The blast is followed by a pressure wave and a black, radioactive rain. Hitting its head hard during the
 *     charge makes it backfire. Firing it costs Godzilla some health.</li>
 *     <li>It fixates on whoever hurt it most recently and hunts them down.</li>
 *     <li><b>Operation Wada Tsumi:</b> while it's in deep water, attach two Freon tanks to sink it (crushing internal
 *     damage), then release a balloon rig to haul it back up. A Shinden with its charge armed, flown into its head while
 *     it's charging the heat ray, makes the beam turn inward.</li>
 *     <li><b>Minus Zero:</b> regenerated from its own cells two years later: larger, more aggressive, heals faster.</li>
 * </ul>
 */
public class MinusOneGodzillaEntity extends KaijuEntity {
    public static final int ODO = 0, MINUS_ONE = 1, MINUS_ZERO = 2;
    private static final EntityDataAccessor<Integer> DATA_SCARS = SynchedEntityData.defineId(MinusOneGodzillaEntity.class, EntityDataSerializers.INT);

    private int lastHeadHit = -1000;
    private float chargeHeadDamage;
    @Nullable
    private java.util.UUID fixation;
    private int fixationTicks;
    private int freonTanks;
    private int sinkTicks;
    private int balloonWindow;
    private int riseTicks;
    private boolean breaksFree;

    public MinusOneGodzillaEntity(EntityType<? extends MinusOneGodzillaEntity> type, Level level) {
        super(type, level);
        this.xpReward = 800;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(700, 18, 10, 30, 0.24);
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
        list.add(new BeamAbility("heat_ray", BeamStyle.HEAT_RAY, 1, 1.5f, ModDamageTypes.HEAT_RAY, 100, 16, 3600) {
            @Override
            public void tickCharge(KaijuEntity k, @Nullable LivingEntity target, int tick) {
                super.tickCharge(k, target, tick);
                ((MinusOneGodzillaEntity) k).dorsalLoading(tick);
            }

            @Override
            public void activate(KaijuEntity k, @Nullable LivingEntity target) {
                super.activate(k, target);
                ((MinusOneGodzillaEntity) k).heatRayFired(aimPoint(k, target));
            }
        }.damage(60f, 4).blocks(3.5f, true).sounds(ModSounds.MINUS_ONE_ROAR, ModSounds.HEAT_RAY_FIRE).chargeParticle(ParticleTypes.SOUL_FIRE_FLAME)
                .nuclear().range(8, 128).initialCooldown(600).when(k -> k.getVariant() != ODO));
        list.add(new RollAbility("swim_charge", "charge", "swim", 2.2).when(k -> k.isInWater() && k.getVariant() != ODO));
        list.add(new TailSwipeAbility());
        list.add(new RoarAbility(ModSounds.MINUS_ONE_ROAR));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 1.5f, 1.5f, new Vec3(0, 8.8, 1.6), 1.4f),
                new KaijuPart(this, "tail", 1.8f, 1.6f, new Vec3(0, 1.4, -5.6), 0.7f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 9.0, 2.4);
    }

    @Override
    protected double variantScale() {
        return switch (getVariant()) {
            case ODO -> 0.32;
            case MINUS_ZERO -> GodzillaConfig.get(GodzillaConfig.MINUS_ZERO_SCALE, 1.2);
            default -> 1.0;
        };
    }

    @Override
    protected void applyVariantAttributes() {
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(switch (getVariant()) {
            case ODO -> 160;
            case MINUS_ZERO -> 900;
            default -> 700;
        });
        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(getVariant() == ODO ? 12 : getVariant() == MINUS_ZERO ? 36 : 30);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SCARS, 0);
    }

    public int getScars() {
        return entityData.get(DATA_SCARS);
    }

    @Override
    public Component getTypeName() {
        return Component.translatable(switch (getVariant()) {
            case ODO -> "entity.reikogodzilla.godzilla_odo";
            case MINUS_ZERO -> "entity.reikogodzilla.godzilla_minus_zero";
            default -> "entity.reikogodzilla.godzilla_minus_one";
        });
    }

    @Override
    protected boolean attacksPlayersOnSight() {
        return getVariant() != ODO || level().isNight();
    }

    // ---------------------------------------------------------------- heat ray

    private boolean chargingHeatRay() {
        return isAbilityActive() && getAbilityPhase() == PHASE_CHARGE && getActiveAbility() != null && getActiveAbility().name.equals("heat_ray");
    }

    /** The plates rise from the tail to the head in three clicks, lock, then glow. */
    private void dorsalLoading(int tick) {
        if (tick == 0) chargeHeadDamage = 0;
        if (!(level() instanceof ServerLevel server)) return;
        float yaw = yBodyRot * Mth.DEG_TO_RAD;
        if (tick == 0 || tick == 20 || tick == 40) {
            int stage = tick / 20;
            server.playSound(null, getX(), getY(), getZ(), SoundEvents.PISTON_EXTEND, SoundSource.HOSTILE, 6f, 0.35f + stage * 0.1f);
            for (int i = 0; i <= 10; i++) {
                double t = (stage * 10 + i) / 30.0;
                Vec3 p = position().add(new Vec3(0, 1.5 + t * 7.0, -6.0 + t * 6.5).scale(getScale()).yRot(-yaw));
                server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 3, 0.3, 0.3, 0.3, 0.02);
            }
        }
        if (tick >= 60 && tick % 3 == 0) {
            for (int i = 0; i < 6; i++) {
                double t = random.nextDouble();
                Vec3 p = position().add(new Vec3(0, 1.5 + t * 7.0, -6.0 + t * 6.5).scale(getScale()).yRot(-yaw));
                server.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 0.2, 0.2, 0.2, 0.01);
                server.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 1, 0.3, 0.3, 0.3, 0.1);
            }
        }
    }

    /** The bolt lands: a mushroom-cloud blast, a ring-shaped pressure wave, then black rain. */
    private void heatRayFired(Vec3 aim) {
        if (!(level() instanceof ServerLevel server)) return;
        Vec3 mouth = getBeamOrigin(0, 1f);
        Vec3 dir = aim.subtract(mouth).normalize();
        Vec3 far = mouth.add(dir.scale(128));
        var hit = server.clip(new net.minecraft.world.level.ClipContext(mouth, far, net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, this));
        Vec3 at = hit.getType() == net.minecraft.world.phys.HitResult.Type.MISS ? aim : hit.getLocation();
        if (at.distanceTo(mouth) > aim.distanceTo(mouth) + 2) at = aim;
        boolean grief = KaijuDestruction.canGrief(server, this);
        server.explode(this, at.x, at.y, at.z, 8f, grief, grief ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
        if (grief) KaijuDestruction.breakSphere(server, at, 9f * Math.max(1f, getScale()), 1600, true, random);
        server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, at.x, at.y + 2, at.z, 4, 3, 3, 3, 0);
        server.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, at.x, at.y + 6, at.z, 120, 6, 14, 6, 0.04);
        // The pressure wave.
        double wave = 40;
        DamageSource pressure = ModDamageTypes.source(server, ModDamageTypes.PRESSURE_WAVE, this);
        for (LivingEntity e : server.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(wave), e -> e != this && e.isAlive())) {
            double d = e.position().distanceTo(at);
            if (d > wave || e instanceof Player p && (p.isCreative() || p.isSpectator())) continue;
            Vec3 away = e.position().subtract(at).multiply(1, 0, 1).normalize();
            float strength = (float) (1 - d / wave);
            e.push(away.x * 3 * strength, 0.6 * strength, away.z * 3 * strength);
            e.hurtMarked = true;
            KaijuDestruction.hurtIgnoringCooldown(e, pressure, (e instanceof KaijuEntity ? 40f : 16f) * strength);
        }
        for (int i = 0; i < 48; i++) {
            double a = Math.PI * 2 * i / 48;
            server.sendParticles(ParticleTypes.CLOUD, at.x + Math.cos(a) * 12, at.y + 1, at.z + Math.sin(a) * 12, 3, 1, 0.5, 1, 0.4);
        }
        if (grief) {
            for (int i = 0; i < 500; i++) {
                BlockPos p = BlockPos.containing(at.add((random.nextDouble() - 0.5) * 48, (random.nextDouble() - 0.3) * 20, (random.nextDouble() - 0.5) * 48));
                var s = server.getBlockState(p);
                if (s.is(net.neoforged.neoforge.common.Tags.Blocks.GLASS_BLOCKS) || s.is(net.neoforged.neoforge.common.Tags.Blocks.GLASS_PANES)
                        || s.is(net.minecraft.tags.BlockTags.LEAVES) || s.is(net.minecraft.tags.BlockTags.FLOWERS)) server.destroyBlock(p, false, this);
            }
        }
        com.reiko45.godzilla.world.Fallout.blackRain(server, at, 20f, 3600, this);
        // Venting the heat costs it.
        hurt(ModDamageTypes.source(server, ModDamageTypes.OVERLOAD, this), fractionOfDesignHealth(0.05f));
        server.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY(0.7), getZ(), 60, getBbWidth() * 0.5, getBbHeight() * 0.3, getBbWidth() * 0.5, 0.05);
    }

    // ---------------------------------------------------------------- regeneration, fixation, mutation

    @Override
    protected float regenPerSecond() {
        return 0f;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel server)) return;
        if (tickCount % 20 == 0 && tickCount - lastHeadHit > 60 && !isCadmiumSuppressed() && getHealth() < getMaxHealth() && isAlive()) {
            float rate = getVariant() == MINUS_ZERO ? 0.025f : 0.02f;
            if (!regenBlocked()) {
                heal(getMaxHealth() * rate * Math.max(0.4f, 1f - 0.1f * getScars()));
                if (tickCount % 60 == 0) server.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY(0.6), getZ(), 4, getBbWidth() * 0.4, getBbHeight() * 0.3, getBbWidth() * 0.4, 0);
            }
        }
        // Fixation on the last thing that hurt it badly.
        if (fixationTicks > 0) {
            fixationTicks--;
            Entity f = fixation == null ? null : server.getEntity(fixation);
            if (f instanceof LivingEntity l && l.isAlive() && (!(l instanceof Player p) || !p.isCreative())) {
                if (getTarget() != l) setTarget(l);
            } else {
                fixationTicks = 0;
            }
        }
        // The Bikini Atoll tests: the island creature absorbs a bomb's worth of radiation and becomes Godzilla.
        if (getVariant() == ODO && tickCount % 40 == 0) {
            BlockPos source = com.reiko45.godzilla.world.Radiation.findNearest(server, position(), 24);
            if (source != null && com.reiko45.godzilla.world.Radiation.drain(server, source, getBoundingBox().getCenter()) != null) {
                setVariant(MINUS_ONE);
                refreshDimensions();
                setHealth(getMaxHealth());
                server.sendParticles(ParticleTypes.FLASH, getX(), getY(0.5), getZ(), 3, 0, 0, 0, 0);
                triggerAnim("action", "roar");
                playSound(ModSounds.MINUS_ONE_ROAR.get(), 12f, 0.8f);
                for (ServerPlayer p : server.players()) {
                    if (p.distanceToSqr(this) < 256 * 256) p.sendSystemMessage(Component.translatable("message.reikogodzilla.godzilla_mutates").withStyle(ChatFormatting.AQUA));
                }
            }
        }
    }

    /** Operation Wada Tsumi ticks here: Godzilla is stunned while he sinks and rises, and stunned mobs skip their AI step. */
    @Override
    public void aiStep() {
        if (!level().isClientSide && isAlive() && level() instanceof ServerLevel server) tickWadaTsumi(server);
        super.aiStep();
    }

    private int regenBlockedUntil;

    private boolean regenBlocked() {
        return tickCount < regenBlockedUntil;
    }

    /** Wounds from the inside heal much more slowly. */
    public void internalDamage(float fraction, int noRegenTicks, net.minecraft.resources.ResourceKey<net.minecraft.world.damagesource.DamageType> type) {
        if (!(level() instanceof ServerLevel server)) return;
        regenBlockedUntil = Math.max(regenBlockedUntil, tickCount + noRegenTicks);
        hurt(ModDamageTypes.source(server, type, null), fractionOfDesignHealth(fraction));
    }

    @Override
    public boolean hurtFromPart(KaijuPart part, DamageSource source, float amount) {
        if (part.name.equals("head")) {
            lastHeadHit = tickCount;
            if (chargingHeatRay()) {
                chargeHeadDamage += amount * part.damageMultiplier;
                if (chargeHeadDamage > getMaxHealth() * 0.15f) backfire();
            }
            if (source.is(DamageTypeTags.IS_EXPLOSION)) regenBlockedUntil = Math.max(regenBlockedUntil, tickCount + 400);
        }
        return super.hurtFromPart(part, source, amount);
    }

    /** Disrupted mid-charge, the heat goes the wrong way. */
    private void backfire() {
        stopAbility();
        if (level() instanceof ServerLevel server) {
            Vec3 mouth = getBeamOrigin(0, 1f);
            server.explode(this, mouth.x, mouth.y, mouth.z, 3f, Level.ExplosionInteraction.NONE);
            internalDamage(0.12f, 600, ModDamageTypes.OVERLOAD);
            for (ServerPlayer p : server.players()) {
                if (p.distanceToSqr(this) < 192 * 192) p.displayClientMessage(Component.translatable("message.reikogodzilla.heat_ray_backfire"), true);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        float before = getHealth();
        boolean hurt = super.hurt(source, amount);
        if (hurt && !level().isClientSide) {
            float dealt = before - getHealth();
            if (dealt > getMaxHealth() * 0.15f && getScars() < 6) entityData.set(DATA_SCARS, getScars() + 1);
            if (source.getEntity() instanceof LivingEntity attacker && attacker != this
                    && (fixationTicks <= 0 || dealt > getMaxHealth() * 0.03f)) {
                fixation = attacker.getUUID();
                fixationTicks = 300;
            }
        }
        return hurt;
    }

    // ---------------------------------------------------------------- Operation Wada Tsumi and the Shinden

    /** A Freon tank cable attached by a ship (or a player). Two release the Freon and sink it. */
    public boolean attachFreon(@Nullable Player by) {
        if (!isInWater() || sinkTicks > 0 || riseTicks > 0) {
            if (by != null) by.displayClientMessage(Component.translatable("message.reikogodzilla.freon_needs_water"), true);
            return false;
        }
        freonTanks++;
        if (by != null) by.displayClientMessage(Component.translatable("message.reikogodzilla.freon_attached", freonTanks), true);
        if (freonTanks >= 2) {
            freonTanks = 0;
            sinkTicks = 120;
            balloonWindow = 1600;
            stun(130);
            playSound(SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE, 8f, 0.4f);
            internalDamage(0.3f, 2400, ModDamageTypes.DECOMPRESSION);
            announceNear("message.reikogodzilla.wada_tsumi_sink");
        }
        return true;
    }

    /** The balloon rig: haul it back up from the depths. It might claw its way free halfway. */
    public boolean releaseBalloons(@Nullable Player by) {
        if (balloonWindow <= 0 || riseTicks > 0) {
            if (by != null) by.displayClientMessage(Component.translatable("message.reikogodzilla.balloons_not_ready"), true);
            return false;
        }
        balloonWindow = 0;
        riseTicks = 100;
        breaksFree = random.nextBoolean();
        stun(110);
        announceNear("message.reikogodzilla.wada_tsumi_rise");
        return true;
    }

    private void tickWadaTsumi(ServerLevel server) {
        if (balloonWindow > 0) balloonWindow--;
        if (sinkTicks > 0) {
            sinkTicks--;
            setDeltaMovement(getDeltaMovement().multiply(0.2, 1, 0.2).add(0, -0.6, 0));
            hurtMarked = true;
            if (tickCount % 3 == 0) server.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, getX(), getY(0.5), getZ(), 20, getBbWidth() * 0.6, getBbHeight() * 0.4, getBbWidth() * 0.6, 0.1);
        }
        if (riseTicks > 0) {
            riseTicks--;
            if (breaksFree && riseTicks == 50) {
                riseTicks = 0;
                internalDamage(0.1f, 1200, ModDamageTypes.DECOMPRESSION);
                announceNear("message.reikogodzilla.wada_tsumi_free");
                return;
            }
            setDeltaMovement(getDeltaMovement().multiply(0.2, 0, 0.2).add(0, 0.7, 0));
            hurtMarked = true;
            if (tickCount % 3 == 0) server.sendParticles(ParticleTypes.BUBBLE, getX(), getY(0.5), getZ(), 30, getBbWidth() * 0.6, getBbHeight() * 0.4, getBbWidth() * 0.6, 0.1);
            if (riseTicks == 0) {
                internalDamage(0.2f, 2400, ModDamageTypes.DECOMPRESSION);
                announceNear("message.reikogodzilla.wada_tsumi_done");
            }
        }
    }

    /** An armed Shinden flown into its head. Mid-charge, the heat ray turns inward. */
    public void overload(@Nullable Entity cause) {
        if (!(level() instanceof ServerLevel server)) return;
        boolean charging = chargingHeatRay();
        if (charging) stopAbility();
        regenBlockedUntil = Math.max(regenBlockedUntil, tickCount + 3600);
        Vec3 head = getBeamOrigin(0, 1f);
        server.explode(this, head.x, head.y, head.z, 4f, Level.ExplosionInteraction.NONE);
        server.sendParticles(ParticleTypes.END_ROD, head.x, head.y, head.z, 80, 2, 2, 2, 0.3);
        hurt(ModDamageTypes.source(server, ModDamageTypes.OVERLOAD, cause), fractionOfDesignHealth(charging ? 1.25f : 0.4f));
        announceNear(charging ? "message.reikogodzilla.overload_inward" : "message.reikogodzilla.overload");
    }

    private void announceNear(String key) {
        if (!(level() instanceof ServerLevel server)) return;
        for (ServerPlayer p : server.players()) {
            if (p.distanceToSqr(this) < 192 * 192) p.sendSystemMessage(Component.translatable(key).withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        }
    }

    @Nullable
    @Override
    public float[] getTint() {
        float[] base = super.getTint();
        if (base != null) return base;
        int scars = getScars();
        if (scars == 0) return null;
        float f = 1f - 0.04f * scars;
        return new float[]{f + 0.05f, f, f};
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Scars", getScars());
        tag.putInt("FreonTanks", freonTanks);
        tag.putInt("BalloonWindow", balloonWindow);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_SCARS, tag.getInt("Scars"));
        freonTanks = tag.getInt("FreonTanks");
        balloonWindow = tag.getInt("BalloonWindow");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.MINUS_ONE_ROAR.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MINUS_ONE_ROAR.get();
    }
}

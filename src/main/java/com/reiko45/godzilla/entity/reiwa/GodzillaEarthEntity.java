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
 * Godzilla Earth and Godzilla Filius (the anime trilogy, 2017-2018). Filius is a 50 m cell-divided offspring; Godzilla
 * Earth has grown for twenty thousand years to 300 m and rules the planet.
 * <ul>
 *     <li><b>Asymmetrically permeable shield:</b> an electromagnetic field soaks up every attack except blows from
 *     very close and hits to the dorsal fins (the organ that makes it). Every 45 seconds the organ recycles and the
 *     shield drops for three seconds (watch for the crackle).</li>
 *     <li><b>EMP probes:</b> plant three in the fins while the shield is down. Filius explodes from within; Godzilla
 *     Earth is badly hurt.</li>
 *     <li>Its charged-particle breath reaches enormous distances, and Godzilla Earth snipes anything flying high
 *     overhead. Pin it down (liquid nanometal) and it vents heat that melts the trap.</li>
 *     <li>A Titan: it ignores people unless they attack it. Filius slowly grows into Godzilla Earth.</li>
 * </ul>
 */
public class GodzillaEarthEntity extends KaijuEntity {
    public static final int FILIUS = 0, EARTH = 1;
    private static final EntityDataAccessor<Boolean> DATA_SHIELD_DOWN = SynchedEntityData.defineId(GodzillaEarthEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int CYCLE = 900, WINDOW = 60;
    private int cycle;
    private int age;
    private int probes;
    private int probeTimer;
    private boolean finHit;
    private int ventTicks;
    private int ventCooldown;

    public GodzillaEarthEntity(EntityType<? extends GodzillaEarthEntity> type, Level level) {
        super(type, level);
        this.xpReward = 2000;
        this.cycle = random.nextInt(CYCLE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(1200, 24, 16, 34, 0.2);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.TITAN;
    }

    @Override
    public boolean isGodzilla() {
        return true;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("atomic_breath", BeamStyle.CHARGED_PARTICLE, 1, 1.4f, ModDamageTypes.CHARGED_PARTICLE, 120, 50, 2400)
                .damage(30f, 4).blocks(3.5f, true).sounds(ModSounds.GODZILLA_EARTH_ROAR, ModSounds.ATOMIC_BREATH_FIRE)
                .chargeParticle(ParticleTypes.ELECTRIC_SPARK).nuclear().range(8, 72));
        list.add(new TailSwipeAbility());
        list.add(new SonicWaveAbility(6f));
        list.add(new RoarAbility(ModSounds.GODZILLA_EARTH_ROAR));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 1.6f, 1.6f, new Vec3(0, 9.0, 1.6), 1.2f),
                new KaijuPart(this, "fins", 2.4f, 3.2f, new Vec3(0, 6.0, -1.6), 1.5f),
                new KaijuPart(this, "tail", 1.8f, 1.6f, new Vec3(0, 1.4, -6.0), 0.7f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 9.2, 2.2);
    }

    @Override
    protected double variantScale() {
        return getVariant() == EARTH ? GodzillaConfig.get(GodzillaConfig.GODZILLA_EARTH_SCALE, 4.8) : 1.0;
    }

    @Override
    protected void applyVariantAttributes() {
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(getVariant() == EARTH ? 6000 : 1200);
        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(getVariant() == EARTH ? 60 : 34);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SHIELD_DOWN, false);
    }

    public boolean isShieldDown() {
        return entityData.get(DATA_SHIELD_DOWN);
    }

    @Override
    public Component getTypeName() {
        return Component.translatable(getVariant() == EARTH ? "entity.reikogodzilla.godzilla_earth" : "entity.reikogodzilla.godzilla_filius");
    }

    @Override
    protected boolean attacksPlayersOnSight() {
        return false;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel server)) return;
        // The shield organ's cycle.
        cycle = (cycle + 1) % CYCLE;
        boolean down = cycle >= CYCLE - WINDOW;
        if (down != isShieldDown()) entityData.set(DATA_SHIELD_DOWN, down);
        if (cycle >= CYCLE - WINDOW - 30 && cycle < CYCLE - WINDOW && cycle % 5 == 0) {
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY(0.6), getZ(), 30, getBbWidth() * 0.6, getBbHeight() * 0.4, getBbWidth() * 0.6, 0.3);
            if (cycle % 10 == 0) playSound(SoundEvents.BEACON_DEACTIVATE, 4f, 1.6f);
        }
        if (probeTimer > 0 && --probeTimer == 0) probes = 0;
        // Filius grows into Godzilla Earth.
        if (getVariant() == FILIUS && ++age >= GodzillaConfig.get(GodzillaConfig.FILIUS_GROWTH_TICKS, 480000)) {
            setVariant(EARTH);
            refreshDimensions();
            setHealth(getMaxHealth());
            playSound(ModSounds.GODZILLA_EARTH_ROAR.get(), 16f, 0.6f);
        }
        // Godzilla Earth shoots down anything flying high overhead.
        if (getVariant() == EARTH && tickCount % 100 == 0) {
            var flyers = server.getEntities(this, getBoundingBox().inflate(256, 256, 256), e -> e.getY() > getY() + getBbHeight() * 0.8
                    && InstantBeam.isAirborne(e) && !(e instanceof KaijuPart) && !(e instanceof net.minecraft.world.entity.projectile.Projectile)
                    && (e instanceof LivingEntity l && (wantsToAttack(l) || l == getLastHurtByMob() || l.getVehicle() == getLastHurtByMob())));
            if (!flyers.isEmpty()) {
                Entity t = flyers.get(0);
                InstantBeam.fire(server, this, getBeamOrigin(0, 1f), t.getBoundingBox().getCenter(), InstantBeam.dust(0.6f, 0.85f, 1f, 2.5f),
                        ModDamageTypes.source(server, ModDamageTypes.CHARGED_PARTICLE, this), 40f);
                playSound(ModSounds.ATOMIC_BREATH_FIRE.get(), 8f, 1.4f);
            }
        }
    }

    /** Heat venting when pinned down or badly hurt. Ticks here, since a stunned mob skips its AI step. */
    @Override
    public void aiStep() {
        if (!level().isClientSide && isAlive() && level() instanceof ServerLevel server) {
            if (ventCooldown > 0) ventCooldown--;
            if (ventTicks <= 0 && ventCooldown <= 0 && (isStunned() || getHealth() < getMaxHealth() * 0.35f)) {
                ventTicks = 80;
                ventCooldown = 600;
                playSound(SoundEvents.LAVA_EXTINGUISH, 8f, 0.5f);
            }
            if (ventTicks > 0) vent(server, --ventTicks);
        }
        super.aiStep();
    }

    /** Above 1000 degrees: everything nearby burns, sand turns to glass, water boils off, nanometal melts. */
    private void vent(ServerLevel server, int tick) {
        double radius = 10 + getBbWidth() * 0.5;
        if (tick % 5 == 0) {
            server.sendParticles(ParticleTypes.FLAME, getX(), getY(0.5), getZ(), 40, radius * 0.5, getBbHeight() * 0.3, radius * 0.5, 0.05);
            server.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY(0.8), getZ(), 20, radius * 0.4, getBbHeight() * 0.2, radius * 0.4, 0.05);
            for (LivingEntity e : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius), e -> e != this && !isAlliedTo(e))) {
                if (e instanceof Player p && (p.isCreative() || p.isSpectator())) continue;
                e.igniteForSeconds(5);
                KaijuDestruction.hurtIgnoringCooldown(e, server.damageSources().inFire(), e instanceof KaijuEntity ? 20f : 6f);
            }
        }
        if (tick % 4 == 0) {
            BlockPos c = blockPosition();
            int r = (int) radius + 6;
            for (int i = 0; i < 120; i++) {
                BlockPos p = c.offset(random.nextInt(r * 2 + 1) - r, random.nextInt(12) - 3, random.nextInt(r * 2 + 1) - r);
                var s = server.getBlockState(p);
                if (s.getBlock() instanceof com.reiko45.godzilla.block.LiquidNanometalBlock || s.getBlock() instanceof com.reiko45.godzilla.block.NanometalBlock) {
                    server.setBlock(p, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
                    server.sendParticles(ParticleTypes.LAVA, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 2, 0.2, 0.2, 0.2, 0);
                } else if (KaijuDestruction.canGrief(server, this)) {
                    if (s.is(net.minecraft.tags.BlockTags.SAND)) server.setBlock(p, net.minecraft.world.level.block.Blocks.GLASS.defaultBlockState(), 3);
                    else if (s.is(net.minecraft.world.level.block.Blocks.WATER)) server.setBlock(p, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    // ---------------------------------------------------------------- the shield

    @Override
    public boolean hurtFromPart(KaijuPart part, DamageSource source, float amount) {
        finHit = part.name.equals("fins");
        try {
            return super.hurtFromPart(part, source, amount);
        } finally {
            finHit = false;
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide && !isShieldDown() && !finHit && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            Entity attacker = source.getEntity();
            boolean closeBlow = attacker != null && source.getDirectEntity() == attacker
                    && attacker.getBoundingBox().inflate(4).intersects(getBoundingBox());
            if (!closeBlow) {
                if (level() instanceof ServerLevel server && tickCount % 4 == 0) {
                    Vec3 from = source.getSourcePosition() != null ? source.getSourcePosition() : position();
                    Vec3 hit = getBoundingBox().getCenter().lerp(from, 0.3);
                    server.sendParticles(ParticleTypes.ELECTRIC_SPARK, hit.x, hit.y, hit.z, 16, 1.0, 1.0, 1.0, 0.3);
                    server.playSound(null, hit.x, hit.y, hit.z, SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 3f, 0.5f);
                }
                if (attacker instanceof LivingEntity l && l != this) setLastHurtByMob(l);
                return false;
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public void onSpecialPayload(com.reiko45.godzilla.entity.projectile.KaijuMissileEntity.Payload payload, @Nullable String part, @Nullable Entity owner) {
        if (payload != com.reiko45.godzilla.entity.projectile.KaijuMissileEntity.Payload.EMP_PROBE || !(level() instanceof ServerLevel server)) return;
        if (!"fins".equals(part) || !isShieldDown()) {
            if (owner instanceof ServerPlayer p) p.displayClientMessage(Component.translatable("message.reikogodzilla.probe_bounced"), true);
            return;
        }
        probes++;
        probeTimer = 600;
        if (owner instanceof ServerPlayer p) p.displayClientMessage(Component.translatable("message.reikogodzilla.probe_attached", probes), true);
        if (probes < 3) return;
        probes = 0;
        server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(0.6), getZ(), 3, getBbWidth() * 0.3, getBbHeight() * 0.2, getBbWidth() * 0.3, 0);
        Emp(server);
        for (ServerPlayer pl : server.players()) {
            if (pl.distanceToSqr(this) < 256 * 256) pl.sendSystemMessage(Component.translatable(getVariant() == FILIUS
                    ? "message.reikogodzilla.filius_destroyed" : "message.reikogodzilla.earth_overloaded").withStyle(ChatFormatting.AQUA));
        }
        if (getVariant() == FILIUS) hurt(server.damageSources().genericKill(), Float.MAX_VALUE);
        else hurt(ModDamageTypes.source(server, ModDamageTypes.EM_SHIELD, owner), fractionOfDesignHealth(0.2f));
    }

    private void Emp(ServerLevel server) {
        com.reiko45.godzilla.world.Emp.pulse(server, position(), 24 + getBbWidth(), 200, true);
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
        return ModSounds.GODZILLA_EARTH_ROAR.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GODZILLA_EARTH_ROAR.get();
    }
}

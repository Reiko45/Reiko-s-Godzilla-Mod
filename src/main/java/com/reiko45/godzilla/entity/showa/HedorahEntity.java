package com.reiko45.godzilla.entity.showa;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.entity.projectile.AcidSapEntity;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Hedorah, the Smog Monster (Godzilla vs. Hedorah, 1971): an alien mineral lifeform that feeds on pollution.
 * <p>
 * It grows through four stages as it eats smoke and filth (lit furnaces, smokers, campfires, fire, lava and magma
 * nearby): a tadpole in the water, a crawling land form, a flying saucer that rains acid mist, and the towering
 * Perfect form with the Hedrium Ray. Hit hard enough and it sheds chunks of sludge that crawl back to rejoin it.
 * When the Perfect form is nearly beaten it turns back into the flying form to escape.
 * <p>
 * Its weakness is drying out: it never heals in hot, dry places and takes extra damage there, and powered Electrode
 * Plates nearby dry it to ash. Water and rain heal it quickly.
 */
public class HedorahEntity extends FlyingKaijuEntity {
    public static final int AQUATIC = 0;
    public static final int LAND = 1;
    public static final int FLYING = 2;
    public static final int PERFECT = 3;
    public static final int FINAL_WARS = 4;
    private static final double[] HEALTH = {150, 350, 500, 1000, 1500};
    private static final double[] ATTACK = {6, 14, 16, 26, 34};
    private static final int[] GROW_AT = {20, 50, 90};
    private static final EntityDimensions[] DIMENSIONS = {
            EntityDimensions.scalable(2.0f, 1.4f).withEyeHeight(1.0f),
            EntityDimensions.scalable(3.0f, 6.0f).withEyeHeight(5.0f),
            EntityDimensions.scalable(6.0f, 2.2f).withEyeHeight(1.6f),
            EntityDimensions.scalable(4.0f, 12.0f).withEyeHeight(10.6f),
            EntityDimensions.scalable(4.0f, 12.0f).withEyeHeight(10.6f)};
    private static final DustParticleOptions SLUDGE = new DustParticleOptions(new Vector3f(0.3f, 0.32f, 0.25f), 2.0f);
    private static final DustParticleOptions ASH = new DustParticleOptions(new Vector3f(0.6f, 0.6f, 0.58f), 1.5f);

    private static final EntityDataAccessor<Boolean> DATA_FRAGMENT = SynchedEntityData.defineId(HedorahEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_DRYING = SynchedEntityData.defineId(HedorahEntity.class, EntityDataSerializers.BOOLEAN);

    private int growth;
    private int dryingTicks;
    private int escapeTicks;
    @Nullable
    private UUID parentId;

    public HedorahEntity(EntityType<? extends HedorahEntity> type, Level level) {
        super(type, level);
        this.xpReward = 400;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(150, 10, 4, 6, 0.22).add(Attributes.FLYING_SPEED, 0.45);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    public boolean hasMBase() {
        return getVariant() == FINAL_WARS;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new SprayAbility("sludge", k -> new AcidSapEntity(k.level(), k), ModSounds.HEDORAH_CRY, 1.1f, 6f, 30, 180)
                .interval(5).when(k -> k.getVariant() != FLYING && !((HedorahEntity) k).isFragment()));
        list.add(new AcidMistAbility(8f).when(k -> k.getVariant() >= FLYING));
        list.add(new BeamAbility("hedrium_ray", BeamStyle.HEDRIUM, 1, 0.5f, ModDamageTypes.HEDRIUM_RAY, 25, 45, 240)
                .damage(10f, 4).blocks(1.5f, true).chargeParticle(ParticleTypes.CRIMSON_SPORE)
                .range(4, 44).when(k -> k.getVariant() >= FLYING));
        list.add(new RoarAbility(ModSounds.HEDORAH_CRY).when(k -> !((HedorahEntity) k).isFragment()));
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return DIMENSIONS[Math.min(getVariant(), DIMENSIONS.length - 1)];
    }

    @Override
    protected double variantScale() {
        if (isFragment()) return 0.35;
        return getVariant() == FINAL_WARS ? 2.0 : 1.0;
    }

    @Override
    protected void applyVariantAttributes() {
        int v = Math.min(getVariant(), HEALTH.length - 1);
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(isFragment() ? 20 : HEALTH[v]);
        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(isFragment() ? 3 : ATTACK[v]);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FRAGMENT, false);
        builder.define(DATA_DRYING, false);
    }

    public boolean isFragment() {
        return entityData.get(DATA_FRAGMENT);
    }

    public boolean isDrying() {
        return entityData.get(DATA_DRYING);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_FRAGMENT.equals(key)) refreshDimensions();
    }

    @Override
    public Component getTypeName() {
        if (isFragment()) return Component.translatable("entity.reikogodzilla.hedorah_fragment");
        return Component.translatable(switch (getVariant()) {
            case AQUATIC -> "entity.reikogodzilla.hedorah_aquatic";
            case LAND -> "entity.reikogodzilla.hedorah_land";
            case FLYING -> "entity.reikogodzilla.hedorah_flying";
            case FINAL_WARS -> "entity.reikogodzilla.hedorah_final_wars";
            default -> "entity.reikogodzilla.hedorah";
        });
    }

    // ---------------------------------------------------------------- flight (only the saucer form flies)

    @Override
    public boolean canFlyNow() {
        return getVariant() == FLYING && !isFragment();
    }

    @Override
    public boolean alwaysFlies() {
        return getVariant() == FLYING && !isFragment();
    }

    @Override
    public boolean prefersFlightInCombat() {
        return canFlyNow();
    }

    @Override
    public boolean showsBossBar() {
        return !isFragment() && super.showsBossBar();
    }

    // ---------------------------------------------------------------- growth, drying, splitting

    /** Electrode Plates: dries Hedorah out. No healing, double damage, and it slowly crumbles to ash. */
    public void applyDesiccation(int ticks) {
        dryingTicks = Math.max(dryingTicks, ticks);
        entityData.set(DATA_DRYING, true);
    }

    private boolean inDryPlace() {
        if (level().dimensionType().ultraWarm()) return true;
        BlockPos pos = blockPosition();
        return !level().getBiome(pos).value().hasPrecipitation() || level().getBiome(pos).value().getBaseTemperature() > 1.0f;
    }

    private boolean inWet() {
        return isInWaterOrRain();
    }

    @Override
    protected float regenPerSecond() {
        if (isFragment() || isDrying() || inDryPlace()) return 0f;
        return inWet() ? 3.0f : 0.5f;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isDrying()) amount *= 2.0f;
        else if (inDryPlace()) amount *= 1.25f;
        boolean hurt = super.hurt(source, amount);
        if (hurt && !level().isClientSide && isAlive() && !isFragment() && getVariant() >= LAND
                && amount > getMaxHealth() * 0.04f && random.nextInt(3) == 0) {
            int n = 1 + random.nextInt(2);
            for (int i = 0; i < n; i++) shedFragment();
        }
        return hurt;
    }

    /** A chunk of sludge splashes off and tries to crawl back. */
    private void shedFragment() {
        if (!(level() instanceof ServerLevel server)) return;
        HedorahEntity blob = ModEntities.HEDORAH.get().create(server);
        if (blob == null) return;
        blob.entityData.set(DATA_FRAGMENT, true);
        blob.parentId = getUUID();
        blob.setVariant(AQUATIC);
        blob.setHealth(blob.getMaxHealth());
        double a = random.nextDouble() * Math.PI * 2;
        double d = getBbWidth() * 0.8 + 2;
        blob.moveTo(getX() + Math.cos(a) * d, getY() + 0.5, getZ() + Math.sin(a) * d, random.nextFloat() * 360f, 0);
        blob.setDeltaMovement(Math.cos(a) * 0.5, 0.4, Math.sin(a) * 0.5);
        server.addFreshEntity(blob);
        server.sendParticles(SLUDGE, blob.getX(), blob.getY() + 0.5, blob.getZ(), 20, 0.5, 0.5, 0.5, 0.1);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel server)) return;
        if (dryingTicks > 0) {
            dryingTicks--;
            if (tickCount % 20 == 0) {
                hurt(ModDamageTypes.source(server, ModDamageTypes.ACID_MIST, null), Math.max(2f, getMaxHealth() * 0.01f));
                server.sendParticles(ASH, getX(), getY(0.5), getZ(), 30, getBbWidth() * 0.4, getBbHeight() * 0.4, getBbWidth() * 0.4, 0.05);
            }
            if (dryingTicks <= 0) entityData.set(DATA_DRYING, false);
        }
        if (isFragment()) {
            tickFragment(server);
            return;
        }
        if (tickCount % 40 == 0) feedOnPollution(server);
        if (escapeTicks > 0 && --escapeTicks <= 0 && getVariant() == FLYING) reform(PERFECT);
        if (getVariant() == PERFECT && escapeTicks == 0 && getHealth() < getMaxHealth() * 0.2f && getHealth() > 0 && random.nextInt(40) == 0) {
            // Turns back into the flying saucer to get away, then lands and reforms later.
            escapeTicks = 1200;
            reform(FLYING);
            setTarget(null);
            setDestination(blockPosition().offset(random.nextInt(161) - 80, 20, random.nextInt(161) - 80));
        }
        if (getVariant() == FLYING && isFlying() && tickCount % 4 == 0) {
            server.sendParticles(SLUDGE, getX(), getY(), getZ(), 6, getBbWidth() * 0.3, 0.2, getBbWidth() * 0.3, 0.02);
        }
    }

    private void tickFragment(ServerLevel server) {
        HedorahEntity parent = parentId != null && server.getEntity(parentId) instanceof HedorahEntity h && h.isAlive() ? h : null;
        if (parent == null) {
            if (tickCount > 600) {
                server.sendParticles(ASH, getX(), getY(0.5), getZ(), 20, 0.3, 0.3, 0.3, 0.05);
                discard();
            }
            return;
        }
        getNavigation().moveTo(parent, 1.4);
        if (distanceTo(parent) < parent.getBbWidth() * 0.6 + 2.0) {
            parent.heal(parent.getMaxHealth() * 0.03f);
            server.sendParticles(SLUDGE, getX(), getY() + 0.5, getZ(), 20, 0.4, 0.4, 0.4, 0.1);
            discard();
        }
    }

    /** Smoke and filth around it: lit furnaces, smokers, campfires, fire, lava, magma. The Nether is a feast. */
    private void feedOnPollution(ServerLevel server) {
        int found = 0;
        int r = (int) (6 + getBbWidth());
        BlockPos base = blockPosition();
        for (int i = 0; i < 48; i++) {
            BlockPos pos = base.offset(random.nextInt(r * 2 + 1) - r, random.nextInt(9) - 4, random.nextInt(r * 2 + 1) - r);
            BlockState state = server.getBlockState(pos);
            if (isPollution(state)) found++;
        }
        if (server.dimensionType().ultraWarm()) found += 2;
        if (tickCount % 1200 == 0) found++;
        if (found <= 0) return;
        heal(found * 2f);
        int stage = getVariant();
        if (stage >= PERFECT) return;
        growth += (int) Math.ceil(found * GodzillaConfig.get(GodzillaConfig.HEDORAH_GROWTH, 1.0));
        server.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY(0.5), getZ(), 10, getBbWidth() * 0.4, getBbHeight() * 0.4, getBbWidth() * 0.4, 0.02);
        if (growth >= GROW_AT[stage]) reform(stage + 1);
    }

    private static boolean isPollution(BlockState state) {
        if (state.getBlock() instanceof AbstractFurnaceBlock && state.getValue(AbstractFurnaceBlock.LIT)) return true;
        if (state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT)) return true;
        return state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE) || state.is(Blocks.LAVA) || state.is(Blocks.MAGMA_BLOCK);
    }

    private void reform(int stage) {
        if (isAbilityActive()) stopAbility();
        setVariant(stage);
        if (stage != FLYING) setFlying(false);
        setHealth(stage == FLYING && escapeTicks > 0 ? Math.max(getHealth(), getMaxHealth() * 0.4f) : getMaxHealth());
        triggerAnim("action", "roar");
        playSound(ModSounds.HEDORAH_CRY.get(), 6f, 0.7f);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(SLUDGE, getX(), getY(0.5), getZ(), 80, getBbWidth() * 0.6, getBbHeight() * 0.5, getBbWidth() * 0.6, 0.1);
        }
    }

    @Override
    public void finalizeSpawnVariant(MobSpawnType type) {
        if (type == MobSpawnType.EVENT) setVariant(FLYING);
    }

    @Override
    protected void tickDeath() {
        if (deathTime == 0 && level() instanceof ServerLevel server) {
            server.sendParticles(ASH, getX(), getY(0.5), getZ(), 120, getBbWidth() * 0.6, getBbHeight() * 0.5, getBbWidth() * 0.6, 0.05);
        }
        super.tickDeath();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Growth", growth);
        tag.putInt("Drying", dryingTicks);
        tag.putInt("Escape", escapeTicks);
        tag.putBoolean("Fragment", isFragment());
        if (parentId != null) tag.putUUID("Parent", parentId);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(DATA_FRAGMENT, tag.getBoolean("Fragment"));
        super.readAdditionalSaveData(tag);
        growth = tag.getInt("Growth");
        dryingTicks = tag.getInt("Drying");
        entityData.set(DATA_DRYING, dryingTicks > 0);
        escapeTicks = tag.getInt("Escape");
        parentId = tag.hasUUID("Parent") ? tag.getUUID("Parent") : null;
    }

    @Nullable
    @Override
    public float[] getTint() {
        float[] base = super.getTint();
        if (base != null || !isDrying()) return base;
        return new float[]{0.85f, 0.82f, 0.75f};
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isFragment() ? null : ModSounds.HEDORAH_CRY.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return net.minecraft.sounds.SoundEvents.SLIME_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.HEDORAH_CRY.get();
    }
}

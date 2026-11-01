package com.reiko45.godzilla.entity.kaiju;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Godzilla.
 * <ul>
 *     <li>Showa (1954-75): ~50 m, blue atomic breath, tail swipe.</li>
 *     <li>Heisei (1984-95): ~100 m, stronger breath and the nuclear pulse.</li>
 *     <li>Red Spiral Ray (Heisei): gained for a while when a Fire Rodan dies nearby and gives Godzilla its energy.</li>
 *     <li>Burning Godzilla (Heisei): when badly hurt his heart starts melting down. He glows, scorches everything
 *     around him and fires the Infinite Heat Ray. Freezer weapons cool him; if the heat meter fills, he melts down
 *     in an enormous explosion. A Godzilla Junior nearby absorbs the radiation and becomes the new Godzilla.</li>
 * </ul>
 */
public class GodzillaEntity extends KaijuEntity {
    public static final int SHOWA = 0;
    public static final int HEISEI = 1;

    private static final EntityDataAccessor<Boolean> DATA_BURNING = SynchedEntityData.defineId(GodzillaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_HEAT = SynchedEntityData.defineId(GodzillaEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_SPIRAL = SynchedEntityData.defineId(GodzillaEntity.class, EntityDataSerializers.BOOLEAN);

    private int spiralTicks;
    private boolean meltedDown;

    public GodzillaEntity(EntityType<? extends GodzillaEntity> type, Level level) {
        super(type, level);
        this.xpReward = 500;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(900, 20, 10, 28, 0.24);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.TITAN;
    }

    @Override
    public boolean isGodzilla() {
        return true;
    }

    /** The Kilaaks controlled the Showa Godzilla along with the rest of Monster Island (Destroy All Monsters). */
    @Override
    public boolean controllableBy(com.reiko45.godzilla.world.AlienSignal signal) {
        return signal == com.reiko45.godzilla.world.AlienSignal.KILAAK && getVariant() == SHOWA;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("atomic_breath", BeamStyle.ATOMIC, 1, 0.7f, ModDamageTypes.ATOMIC_BREATH, 40, 50, 260)
                .damage(10f, 4).blocks(1.5f, true).sounds(ModSounds.ATOMIC_BREATH_CHARGE, ModSounds.ATOMIC_BREATH_FIRE)
                .chargeParticle(ParticleTypes.SOUL_FIRE_FLAME).nuclear()
                .range(4, 44).when(k -> k.getVariant() == SHOWA));
        list.add(new BeamAbility("atomic_breath", BeamStyle.ATOMIC, 1, 1.0f, ModDamageTypes.ATOMIC_BREATH, 50, 60, 240)
                .damage(14f, 4).blocks(2.2f, true).sounds(ModSounds.ATOMIC_BREATH_CHARGE, ModSounds.ATOMIC_BREATH_FIRE)
                .chargeParticle(ParticleTypes.SOUL_FIRE_FLAME).nuclear()
                .range(4, 44).when(k -> k.getVariant() == HEISEI && !((GodzillaEntity) k).hasRedSpiral() && !((GodzillaEntity) k).isBurning()));
        list.add(new TailSwipeAbility());
        list.add(new RoarAbility(ModSounds.GODZILLA_ROAR));
        list.add(new NuclearPulseAbility().when(k -> k.getVariant() == HEISEI));
        list.add(new BeamAbility("red_spiral_ray", BeamStyle.SPIRAL_RED, 1, 1.1f, ModDamageTypes.RED_SPIRAL_RAY, 40, 60, 220)
                .damage(20f, 4).blocks(2.6f, true).sounds(ModSounds.ATOMIC_BREATH_CHARGE, ModSounds.ATOMIC_BREATH_FIRE)
                .chargeParticle(ParticleTypes.FLAME).nuclear()
                .range(4, 48).when(k -> ((GodzillaEntity) k).hasRedSpiral() && !((GodzillaEntity) k).isBurning()));
        list.add(new BeamAbility("infinite_heat_ray", BeamStyle.INFINITE_HEAT, 1, 1.2f, ModDamageTypes.INFINITE_HEAT_RAY, 30, 70, 160)
                .damage(22f, 4).blocks(3.0f, true).sounds(ModSounds.ATOMIC_BREATH_CHARGE, ModSounds.ATOMIC_BREATH_FIRE)
                .chargeParticle(ParticleTypes.FLAME).nuclear()
                .range(4, 48).when(k -> ((GodzillaEntity) k).isBurning()));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 2.0f, 2.0f, new Vec3(0, 8.3, 2.2), 1.5f),
                new KaijuPart(this, "tail", 2.6f, 2.4f, new Vec3(0, 1.4, -4.2), 0.8f),
                new KaijuPart(this, "tail_tip", 1.8f, 1.6f, new Vec3(0, 0.6, -7.4), 0.6f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 9.0, 2.9);
    }

    @Override
    protected double variantScale() {
        return getVariant() == HEISEI ? 2.0 : 1.0;
    }

    @Override
    protected double baseStepHeight() {
        return 3.0;
    }

    @Override
    protected float regenPerSecond() {
        return getVariant() == HEISEI ? 2.0f : 1.5f;
    }

    @Override
    protected boolean attacksPlayersOnSight() {
        return GodzillaConfig.get(GodzillaConfig.GODZILLA_HOSTILE, false);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BURNING, false);
        builder.define(DATA_HEAT, 0f);
        builder.define(DATA_SPIRAL, false);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData data) {
        setVariant(random.nextInt(2));
        return super.finalizeSpawn(level, difficulty, spawnType, data);
    }

    @Override
    protected void applyVariantAttributes() {
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(getVariant() == HEISEI ? 1024 : 800);
    }

    // ------------------------------------------------------------------ Red Spiral Ray

    public boolean hasRedSpiral() {
        return entityData.get(DATA_SPIRAL);
    }

    /** Fire Rodan's life energy flows into Godzilla: Red Spiral Ray and a big heal. */
    public void absorbRodanEnergy(int ticks) {
        if (getVariant() != HEISEI) setVariant(HEISEI);
        spiralTicks = Math.max(spiralTicks, ticks);
        entityData.set(DATA_SPIRAL, true);
        heal(getMaxHealth() * 0.3f);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.FLAME, getX(), getY(0.5), getZ(), 120, getBbWidth(), getBbHeight() * 0.4, getBbWidth(), 0.1);
        }
        announce("message.reikogodzilla.red_spiral", ChatFormatting.RED);
    }

    // ------------------------------------------------------------------ Burning Godzilla

    public boolean isBurning() {
        return entityData.get(DATA_BURNING);
    }

    public float getHeat() {
        return entityData.get(DATA_HEAT);
    }

    private void startBurning() {
        entityData.set(DATA_BURNING, true);
        entityData.set(DATA_HEAT, 0f);
        roarNow();
        announce("message.reikogodzilla.burning", ChatFormatting.GOLD);
    }

    private void roarNow() {
        level().playSound(null, getX(), getEyeY(), getZ(), ModSounds.GODZILLA_ROAR.get(), net.minecraft.sounds.SoundSource.HOSTILE, 10f, 0.8f);
        triggerAnim("action", "roar");
    }

    @Override
    public void addFreeze(float amount) {
        if (isBurning()) {
            // Freezer weapons fight the meltdown instead of freezing him outright.
            entityData.set(DATA_HEAT, Math.max(0f, getHeat() - amount * 0.6f));
            super.addFreeze(amount * 0.3f);
            return;
        }
        super.addFreeze(amount);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (spiralTicks > 0 && --spiralTicks <= 0) entityData.set(DATA_SPIRAL, false);

        if (!isBurning()) {
            if (getVariant() == HEISEI && getHealth() < getMaxHealth() * 0.35f && GodzillaConfig.get(GodzillaConfig.BURNING_GODZILLA, true)) {
                startBurning();
            }
            return;
        }
        float rate = 100f / GodzillaConfig.get(GodzillaConfig.MELTDOWN_TICKS, 6000);
        entityData.set(DATA_HEAT, Math.min(100f, getHeat() + rate));
        if (getHeat() >= 100f) {
            meltdown(true);
            return;
        }
        if (!(level() instanceof ServerLevel server)) return;
        if (tickCount % 4 == 0) {
            server.sendParticles(ParticleTypes.FLAME, getX(), getY(0.5), getZ(), 10, getBbWidth() * 0.5, getBbHeight() * 0.4, getBbWidth() * 0.5, 0.02);
            server.sendParticles(ParticleTypes.LAVA, getX(), getY(0.6), getZ(), 2, getBbWidth() * 0.4, getBbHeight() * 0.3, getBbWidth() * 0.4, 0.0);
        }
        if (tickCount % 20 == 0) {
            // Radiation and heat pour off his body.
            double radius = 6 * getScale() + getBbWidth();
            DamageSource radiation = ModDamageTypes.source(server, ModDamageTypes.RADIATION, this);
            for (LivingEntity e : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius),
                    e -> e != this && !isAlliedTo(e) && !(e instanceof com.reiko45.godzilla.entity.KaijuAligned a && !getFaction().isHostileTo(a.getFaction()))
                            && !(e instanceof net.minecraft.world.entity.player.Player p && (p.isCreative() || p.isSpectator())))) {
                e.hurt(radiation, 4f);
                if (!e.fireImmune()) e.igniteForSeconds(3);
            }
            if (KaijuDestruction.canGrief(server, this)) {
                for (int i = 0; i < 6; i++) {
                    BlockPos pos = BlockPos.containing(getX() + (random.nextDouble() - 0.5) * radius * 2, getY() + random.nextInt(3),
                            getZ() + (random.nextDouble() - 0.5) * radius * 2);
                    if (server.getBlockState(pos).isAir() && server.getBlockState(pos.below()).isSolidRender(server, pos.below())) {
                        server.setBlockAndUpdate(pos, BaseFireBlock.getState(server, pos));
                    }
                }
            }
        }
        if (tickCount % 600 == 0) {
            for (var p : server.players()) {
                if (p.distanceToSqr(this) < 160 * 160) {
                    p.displayClientMessage(Component.translatable("message.reikogodzilla.heat", Math.round(getHeat())).withStyle(ChatFormatting.GOLD), true);
                }
            }
        }
    }

    @Override
    public void die(DamageSource source) {
        if (isBurning() && !meltedDown && !level().isClientSide) {
            // Killed while burning: if he's been chilled enough the meltdown is contained.
            meltdown(getFreeze() < 50f && !isFrozenSolid());
        }
        super.die(source);
    }

    /** The heart gives out. Optionally explodes; a Godzilla Junior nearby takes in the radiation. */
    private void meltdown(boolean explode) {
        if (meltedDown || !(level() instanceof ServerLevel server)) return;
        meltedDown = true;
        // A Godzilla Junior nearby takes in the radiation first, so it survives the blast as the new Godzilla.
        server.getEntitiesOfClass(GodzillaJuniorEntity.class, getBoundingBox().inflate(96)).stream()
                .min(java.util.Comparator.comparingDouble(this::distanceToSqr))
                .ifPresent(GodzillaJuniorEntity::absorbMeltdown);
        if (explode && GodzillaConfig.get(GodzillaConfig.MELTDOWN_EXPLOSION, true)) {
            boolean grief = KaijuDestruction.canGrief(server, this);
            server.explode(this, ModDamageTypes.source(server, ModDamageTypes.MELTDOWN, this), null, getX(), getY(0.5), getZ(),
                    10f + 2f * getScale(), grief, grief ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
            announce("message.reikogodzilla.meltdown", ChatFormatting.DARK_RED);
        } else {
            announce("message.reikogodzilla.meltdown_contained", ChatFormatting.AQUA);
        }
        server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(0.5), getZ(), 4, getBbWidth(), getBbHeight() * 0.3, getBbWidth(), 0);
        if (isAlive()) {
            entityData.set(DATA_BURNING, false);
            hurt(ModDamageTypes.source(server, ModDamageTypes.MELTDOWN, null), Float.MAX_VALUE);
        }
    }

    private void announce(String key, ChatFormatting color) {
        if (!(level() instanceof ServerLevel server)) return;
        for (var p : server.players()) {
            if (p.distanceToSqr(this) < 192 * 192) p.sendSystemMessage(Component.translatable(key).withStyle(color));
        }
    }

    @Nullable
    @Override
    public float[] getTint() {
        if (isBurning()) {
            float pulse = 0.85f + 0.15f * (float) Math.sin(tickCount * 0.3);
            float heat = getHeat() / 100f;
            return new float[]{1.0f, (0.6f - 0.25f * heat) * pulse + 0.2f, (0.35f - 0.2f * heat) * pulse};
        }
        return super.getTint();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Burning", isBurning());
        tag.putFloat("Heat", getHeat());
        tag.putInt("RedSpiral", spiralTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_BURNING, tag.getBoolean("Burning"));
        entityData.set(DATA_HEAT, tag.getFloat("Heat"));
        spiralTicks = tag.getInt("RedSpiral");
        entityData.set(DATA_SPIRAL, spiralTicks > 0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.GODZILLA_ROAR.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GODZILLA_DEATH.get();
    }
}

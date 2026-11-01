package com.reiko45.godzilla.entity.millennium;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.entity.projectile.KaijuMissileEntity;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The Millennium-era Godzillas, one entity with four forms:
 * <ul>
 *     <li>Godzilla 2000 / vs. Megaguirus (1999-2000): spiky purple-tinged plates, a red-orange breath, and the Organizer
 *     G1 that heals him astonishingly fast.</li>
 *     <li>GMK (2001): the "God of Destruction", possessed by the vengeful dead. White pupil-less eyes, always hostile,
 *     incredibly tough. His weak point is a wound in his shoulder: once it opens (half health, or a bite from GMK King
 *     Ghidorah), D-03 missiles fired into it do enormous damage.</li>
 *     <li>Kiryu-era (2002-03): the Godzilla whose roar awakens the first Godzilla's spirit inside Kiryu.</li>
 *     <li>Final Wars (2004): 100 m tall, fast and agile, with a breath that turns into a red spiral when he's pushed
 *     to the limit.</li>
 * </ul>
 */
public class MillenniumGodzillaEntity extends KaijuEntity {
    public static final int MILLENNIUM = 0;
    public static final int GMK = 1;
    public static final int KIRYU_ERA = 2;
    public static final int FINAL_WARS = 3;
    private static final double[] SCALE = {1.15, 1.25, 1.15, 2.1};
    private static final double[] HEALTH = {1000, 1500, 1000, 1600};
    private static final double[] ATTACK = {30, 34, 30, 38};
    private static final double[] ARMOR = {20, 30, 20, 22};
    private static final DustParticleOptions WOUND = new DustParticleOptions(new Vector3f(0.8f, 0.1f, 0.05f), 1.6f);
    private static final EntityDataAccessor<Boolean> DATA_WOUND = SynchedEntityData.defineId(MillenniumGodzillaEntity.class, EntityDataSerializers.BOOLEAN);
    /** Burning Millennium Godzilla (Fest Godzilla II, 2025-2026): ticks left in the meltdown. */
    private static final EntityDataAccessor<Integer> DATA_BURNING = SynchedEntityData.defineId(MillenniumGodzillaEntity.class, EntityDataSerializers.INT);
    private boolean burnUsed;

    public MillenniumGodzillaEntity(EntityType<? extends MillenniumGodzillaEntity> type, Level level) {
        super(type, level);
        this.xpReward = 550;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(1000, 20, 10, 30, 0.24);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return getVariant() == GMK ? KaijuFaction.INVADER : KaijuFaction.TITAN;
    }

    @Override
    public boolean isGodzilla() {
        return true;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("atomic_breath", BeamStyle.MILLENNIUM, 1, 1.0f, ModDamageTypes.ATOMIC_BREATH, 40, 50, 240)
                .damage(14f, 4).blocks(2.2f, true).sounds(ModSounds.ATOMIC_BREATH_CHARGE, ModSounds.ATOMIC_BREATH_FIRE)
                .styleBy(k -> ((MillenniumGodzillaEntity) k).isBurning() ? BeamStyle.INFINITE_HEAT : BeamStyle.MILLENNIUM)
                .damageScaling(k -> ((MillenniumGodzillaEntity) k).isBurning() ? 1.3 : 1.0)
                .chargeParticle(ParticleTypes.FLAME).nuclear().range(4, 48).when(k -> k.getVariant() == MILLENNIUM));
        list.add(new BeamAbility("atomic_breath", BeamStyle.ATOMIC, 1, 1.1f, ModDamageTypes.ATOMIC_BREATH, 50, 40, 240)
                .damage(18f, 4).blocks(3.2f, true).sounds(ModSounds.ATOMIC_BREATH_CHARGE, ModSounds.ATOMIC_BREATH_FIRE)
                .chargeParticle(ParticleTypes.SOUL_FIRE_FLAME).nuclear().range(4, 52).when(k -> k.getVariant() == GMK));
        list.add(new BeamAbility("atomic_breath", BeamStyle.ATOMIC, 1, 1.0f, ModDamageTypes.ATOMIC_BREATH, 40, 50, 240)
                .damage(14f, 4).blocks(2.2f, true).sounds(ModSounds.ATOMIC_BREATH_CHARGE, ModSounds.ATOMIC_BREATH_FIRE)
                .chargeParticle(ParticleTypes.SOUL_FIRE_FLAME).nuclear().range(4, 48).when(k -> k.getVariant() == KIRYU_ERA));
        list.add(new BeamAbility("atomic_breath", BeamStyle.ATOMIC, 1, 1.4f, ModDamageTypes.ATOMIC_BREATH, 30, 50, 200)
                .damage(20f, 4).blocks(3.0f, true).sounds(ModSounds.ATOMIC_BREATH_CHARGE, ModSounds.ATOMIC_BREATH_FIRE)
                .chargeParticle(ParticleTypes.SOUL_FIRE_FLAME).nuclear().range(4, 64)
                .when(k -> k.getVariant() == FINAL_WARS && k.getHealth() >= k.getMaxHealth() * 0.4f));
        list.add(new BeamAbility("hyper_spiral_burning", BeamStyle.SPIRAL_RED, 1, 1.6f, ModDamageTypes.RED_SPIRAL_RAY, 30, 60, 220)
                .damage(26f, 4).blocks(3.4f, true).sounds(ModSounds.ATOMIC_BREATH_CHARGE, ModSounds.ATOMIC_BREATH_FIRE)
                .chargeParticle(ParticleTypes.FLAME).nuclear().range(4, 64)
                .when(k -> k.getVariant() == FINAL_WARS && k.getHealth() < k.getMaxHealth() * 0.4f));
        list.add(new NuclearPulseAbility());
        list.add(new TailSwipeAbility());
        list.add(new LeapAbility("tail_slam", 1.3, 1.5f, 220).when(k -> k.getVariant() == FINAL_WARS));
        list.add(new RoarAbility(ModSounds.MILLENNIUM_GODZILLA_ROAR));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 2.0f, 2.0f, new Vec3(0, 8.3, 2.2), 1.5f),
                new KaijuPart(this, "shoulder", 1.8f, 1.8f, new Vec3(-1.6, 6.8, 0.2), 1.0f),
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
        return SCALE[Math.min(getVariant(), 3)];
    }

    @Override
    protected double baseStepHeight() {
        return 3.0;
    }

    @Override
    protected void applyVariantAttributes() {
        int v = Math.min(getVariant(), 3);
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(HEALTH[v]);
        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(ATTACK[v] * (isBurning() ? 1.3 : 1.0));
        var armor = getAttribute(Attributes.ARMOR);
        if (armor != null) armor.setBaseValue(ARMOR[v]);
        var speed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.setBaseValue(v == FINAL_WARS ? 0.28 : 0.24);
    }

    /** The Organizer G1: Godzilla 2000 heals extraordinarily fast. */
    @Override
    protected float regenPerSecond() {
        return switch (getVariant()) {
            case MILLENNIUM -> 4.0f;
            case GMK -> 0.5f;
            default -> 1.5f;
        };
    }

    @Override
    protected boolean attacksPlayersOnSight() {
        return getVariant() == GMK || GodzillaConfig.get(GodzillaConfig.GODZILLA_HOSTILE, false);
    }

    @Override
    public Component getTypeName() {
        return Component.translatable(switch (getVariant()) {
            case GMK -> "entity.reikogodzilla.godzilla_gmk";
            case KIRYU_ERA -> "entity.reikogodzilla.godzilla_kiryu_era";
            case FINAL_WARS -> "entity.reikogodzilla.godzilla_final_wars";
            default -> "entity.reikogodzilla.godzilla_millennium";
        });
    }

    // ---------------------------------------------------------------- GMK: the wound

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_WOUND, false);
        builder.define(DATA_BURNING, 0);
    }

    public boolean isBurning() {
        return entityData.get(DATA_BURNING) > 0;
    }

    @Nullable
    @Override
    public float[] getTint() {
        if (isBurning()) return new float[]{1.0f, 0.55f, 0.35f};
        return super.getTint();
    }

    public boolean hasOpenWound() {
        return entityData.get(DATA_WOUND);
    }

    /** King Ghidorah's bite tears open GMK Godzilla's shoulder. */
    public void openWound() {
        if (getVariant() != GMK || hasOpenWound()) return;
        entityData.set(DATA_WOUND, true);
        if (level() instanceof ServerLevel server) {
            for (var p : server.players()) {
                if (p.distanceToSqr(this) < 200 * 200) p.displayClientMessage(Component.translatable("message.reikogodzilla.gmk_wound"), true);
            }
        }
    }

    @Override
    public float missileDamageMultiplier(KaijuMissileEntity.Payload payload, @Nullable String part) {
        if (payload == KaijuMissileEntity.Payload.D03 && hasOpenWound()) return "shoulder".equals(part) ? 14f : 8f;
        return 1f;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && getVariant() == GMK && getHealth() < getMaxHealth() * 0.5f) openWound();
        if (hurt && !level().isClientSide && getVariant() == MILLENNIUM && !burnUsed && isAlive() && getHealth() < getMaxHealth() * 0.25f) {
            // Burning Millennium Godzilla: a last-ditch meltdown that burns through him.
            burnUsed = true;
            entityData.set(DATA_BURNING, 1200);
            applyVariantAttributes();
            triggerAnim("action", "roar");
            playSound(ModSounds.MILLENNIUM_GODZILLA_ROAR.get(), 12f, 0.7f);
            if (level() instanceof ServerLevel server) for (var p : server.players()) {
                if (p.distanceToSqr(this) < 200 * 200) p.sendSystemMessage(Component.translatable("message.reikogodzilla.burning_millennium")
                        .withStyle(net.minecraft.ChatFormatting.RED, net.minecraft.ChatFormatting.ITALIC));
            }
        }
        return hurt;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        int burning = entityData.get(DATA_BURNING);
        if (burning > 0 && level() instanceof ServerLevel server) {
            entityData.set(DATA_BURNING, burning - 1);
            if (burning == 1) applyVariantAttributes();
            if (burning % 10 == 0) {
                double r = 5 * getScale();
                server.sendParticles(ParticleTypes.FLAME, getX(), getY(0.5), getZ(), 30, getBbWidth() * 0.6, getBbHeight() * 0.4, getBbWidth() * 0.6, 0.05);
                for (var e : server.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, getBoundingBox().inflate(r), e -> e != this && !isAlliedTo(e))) {
                    if (e instanceof net.minecraft.world.entity.player.Player p && (p.isCreative() || p.isSpectator())) continue;
                    e.igniteForSeconds(4);
                    com.reiko45.godzilla.util.KaijuDestruction.hurtIgnoringCooldown(e, server.damageSources().inFire(), e instanceof com.reiko45.godzilla.entity.KaijuEntity ? 8f : 3f);
                }
            }
            if (burning % 20 == 0) hurt(server.damageSources().onFire(), fractionOfDesignHealth(0.01f));
        }
        if (hasOpenWound() && tickCount % 10 == 0 && level() instanceof ServerLevel server) {
            Vec3 shoulder = position().add(new Vec3(-1.6, 6.8, 0.2).scale(getScale()).yRot(-yBodyRot * net.minecraft.util.Mth.DEG_TO_RAD));
            server.sendParticles(WOUND, shoulder.x, shoulder.y, shoulder.z, 6, 0.4, 0.4, 0.4, 0.05);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Wound", hasOpenWound());
        tag.putInt("Burning", entityData.get(DATA_BURNING));
        tag.putBoolean("BurnUsed", burnUsed);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_WOUND, tag.getBoolean("Wound"));
        entityData.set(DATA_BURNING, tag.getInt("Burning"));
        burnUsed = tag.getBoolean("BurnUsed");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.MILLENNIUM_GODZILLA_ROAR.get();
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

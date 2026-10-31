package com.reiko45.godzilla.entity.kaiju;

import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.ability.BeamAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.RoarAbility;
import com.reiko45.godzilla.entity.ability.TailSwipeAbility;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Destoroyah (1995): Precambrian crustaceans mutated by the Oxygen Destroyer.
 * <ul>
 *     <li>Aggregate form: forms when enough juveniles swarm together. Walks, bites, spits Micro-Oxygen.</li>
 *     <li>Flying form: evolves after a while (or when badly hurt) and takes to the air.</li>
 *     <li>Perfect form: the 120 m final form, with the Variable Slicer horn blade. Splits into juveniles and
 *     reforms once when near death.</li>
 * </ul>
 * Micro-Oxygen stops regeneration. Weakness: freezer weapons (its micro-oxygen liquefies) fill its freeze meter
 * far faster than other kaiju.
 */
public class DestoroyahEntity extends FlyingKaijuEntity {
    public static final int AGGREGATE = 0;
    public static final int FLYING = 1;
    public static final int PERFECT = 2;
    private static final double[] HEALTH = {400, 600, 1024};
    private static final int[] EVOLVE_TICKS = {3600, 2400};

    private int formTicks;
    private boolean hasSplit;

    public DestoroyahEntity(EntityType<? extends DestoroyahEntity> type, Level level) {
        super(type, level);
        this.xpReward = 700;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(400, 20, 8, 24, 0.26).add(Attributes.FLYING_SPEED, 0.75);
    }

    @Override
    public KaijuFaction getFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("micro_oxygen", BeamStyle.MICRO_OXYGEN, 1, 0.6f, ModDamageTypes.MICRO_OXYGEN, 20, 45, 180)
                .damage(10f, 4).blocks(1.6f, false).chargeParticle(ParticleTypes.WITCH)
                .onHit((k, e) -> {
                    if (e instanceof KaijuEntity kaiju) kaiju.suppressRegeneration(400);
                    else e.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
                })
                .range(4, 48));
        list.add(new BeamAbility("variable_slicer", BeamStyle.MICRO_OXYGEN, 1, 1.0f, ModDamageTypes.MICRO_OXYGEN, 10, 20, 160)
                .damage(20f, 2).blocks(2.0f, false).tracking(0.5f, 0f)
                .range(0, 14).when(k -> k.getVariant() == PERFECT));
        list.add(new TailSwipeAbility().when(k -> k.getVariant() != FLYING));
        list.add(new RoarAbility(ModSounds.DESTOROYAH_ROAR));
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return switch (getVariant()) {
            case AGGREGATE -> EntityDimensions.scalable(6.0f, 6.0f).withEyeHeight(5.0f);
            case FLYING -> EntityDimensions.scalable(5.0f, 5.0f).withEyeHeight(4.0f);
            default -> EntityDimensions.scalable(5.0f, 22.0f).withEyeHeight(20.0f);
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return switch (getVariant()) {
            case AGGREGATE -> new Vec3(0, 4.5, 3.4);
            case FLYING -> new Vec3(0, 3.5, 3.0);
            default -> new Vec3(0, 20.5, 2.6);
        };
    }

    @Override
    protected double baseStepHeight() {
        return getVariant() == PERFECT ? 4.0 : 2.5;
    }

    @Override
    public boolean canFlyNow() {
        return getVariant() != AGGREGATE;
    }

    @Override
    public boolean prefersFlightInCombat() {
        return getVariant() == FLYING;
    }

    @Override
    public float freezeMultiplier() {
        return 2.5f;
    }

    @Override
    protected float regenPerSecond() {
        return 1.0f;
    }

    @Override
    public void setVariant(int form) {
        super.setVariant(form);
        refreshDimensions();
    }

    @Override
    protected void applyVariantAttributes() {
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(HEALTH[Math.max(0, Math.min(2, getVariant()))]);
    }

    private void evolve() {
        int next = getVariant() + 1;
        if (next > PERFECT) return;
        setVariant(next);
        setHealth(getMaxHealth() * 0.8f);
        formTicks = 0;
        if (next == FLYING) setFlying(true);
        if (next == PERFECT) setFlying(false);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(0.5), getZ(), 2, getBbWidth() * 0.5, getBbHeight() * 0.3, getBbWidth() * 0.5, 0);
            server.sendParticles(ParticleTypes.WITCH, getX(), getY(0.5), getZ(), 100, getBbWidth(), getBbHeight() * 0.5, getBbWidth(), 0.2);
            for (var p : server.players()) {
                if (p.distanceToSqr(this) < 160 * 160) {
                    p.sendSystemMessage(Component.translatable(next == FLYING ? "message.reikogodzilla.destoroyah_flying" : "message.reikogodzilla.destoroyah_perfect")
                            .withStyle(ChatFormatting.DARK_PURPLE));
                }
            }
        }
        playSound(ModSounds.DESTOROYAH_ROAR.get(), 10f, 0.8f);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        int form = getVariant();
        if (form < PERFECT) {
            formTicks++;
            if (formTicks >= EVOLVE_TICKS[form] || getHealth() < getMaxHealth() * 0.4f) evolve();
        } else if (!hasSplit && getHealth() < getMaxHealth() * 0.3f) {
            split();
        }
    }

    /** Destoroyah breaks apart into a swarm and reforms. */
    private void split() {
        hasSplit = true;
        if (!(level() instanceof ServerLevel server)) return;
        for (int i = 0; i < 4; i++) {
            DestoroyahJuvenileEntity juvenile = ModEntities.DESTOROYAH_JUVENILE.get().create(server);
            if (juvenile == null) continue;
            double a = i * Math.PI / 2;
            juvenile.moveTo(getX() + Math.cos(a) * getBbWidth(), getY(), getZ() + Math.sin(a) * getBbWidth(), random.nextFloat() * 360f, 0);
            juvenile.finalizeSpawn(server, server.getCurrentDifficultyAt(juvenile.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            server.addFreshEntity(juvenile);
        }
        heal(getMaxHealth() * 0.2f);
        server.sendParticles(ParticleTypes.WITCH, getX(), getY(0.5), getZ(), 150, getBbWidth(), getBbHeight() * 0.5, getBbWidth(), 0.25);
        for (var p : server.players()) {
            if (p.distanceToSqr(this) < 160 * 160) p.sendSystemMessage(Component.translatable("message.reikogodzilla.destoroyah_split").withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("FormTicks", formTicks);
        tag.putBoolean("HasSplit", hasSplit);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        formTicks = tag.getInt("FormTicks");
        hasSplit = tag.getBoolean("HasSplit");
        refreshDimensions();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.DESTOROYAH_ROAR.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.DESTOROYAH_ROAR.get();
    }
}

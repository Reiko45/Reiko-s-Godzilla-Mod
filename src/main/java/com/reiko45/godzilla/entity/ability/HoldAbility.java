package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.registry.ModDamageTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Seizes the target and keeps hurting it for a few seconds: Gigan's buzzsaw (held in front), Manda's coils (crushed
 * against the body), Orga swallowing Godzilla (held in the mouth), Keizer Ghidorah's draining bite, Megaguirus's
 * stinger. Some holds heal the attacker with part of the damage.
 */
public class HoldAbility extends KaijuAbility {
    public enum Mode { FRONT, BODY, MOUTH }

    private final Mode mode;
    private final int interval;
    private final float damageMultiplier;
    private final float healFraction;
    @Nullable
    private ResourceKey<DamageType> damageType;
    @Nullable
    private Supplier<SoundEvent> sound;
    @Nullable
    private BiConsumer<KaijuEntity, LivingEntity> onHit;
    @Nullable
    private BiConsumer<KaijuEntity, LivingEntity> onRelease;
    private float maxSizeRatio = 2.0f;
    @Nullable
    private LivingEntity held;

    public HoldAbility(String name, String anim, Mode mode, int duration, int interval, float damageMultiplier, float healFraction, int cooldown) {
        super(name, 8, duration, cooldown, "charge", anim);
        this.mode = mode;
        this.interval = Math.max(1, interval);
        this.damageMultiplier = damageMultiplier;
        this.healFraction = healFraction;
        this.minRange = 0;
        this.maxRange = 6;
        this.useChance = 0.04f;
    }

    public HoldAbility damageType(ResourceKey<DamageType> type) {
        this.damageType = type;
        return this;
    }

    public HoldAbility sound(Supplier<SoundEvent> sound) {
        this.sound = sound;
        return this;
    }

    /** Extra effect each time the hold hurts its victim. */
    public HoldAbility onHit(BiConsumer<KaijuEntity, LivingEntity> effect) {
        this.onHit = effect;
        return this;
    }

    /** What happens when the victim is let go (thrown, spat out...). */
    public HoldAbility onRelease(BiConsumer<KaijuEntity, LivingEntity> effect) {
        this.onRelease = effect;
        return this;
    }

    /** The biggest victim, relative to the attacker's height, that this hold can take. */
    public HoldAbility maxSize(float ratio) {
        this.maxSizeRatio = ratio;
        return this;
    }

    /** Whatever {@code kaiju} is holding right now, or null. */
    @Nullable
    public static LivingEntity heldBy(KaijuEntity kaiju) {
        return kaiju.getActiveAbility() instanceof HoldAbility hold && kaiju.getAbilityPhase() == KaijuEntity.PHASE_ACTIVE ? hold.held : null;
    }

    @Override
    protected boolean extraStartCheck(KaijuEntity kaiju, LivingEntity target) {
        return target.getBbHeight() <= kaiju.getBbHeight() * maxSizeRatio;
    }

    @Override
    public void tickCharge(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (target != null) faceTowards(kaiju, target.getX(), target.getZ(), 15f);
    }

    @Override
    public void activate(KaijuEntity kaiju, @Nullable LivingEntity target) {
        held = target != null && target.isAlive() && kaiju.distanceTo(target) < kaiju.getMeleeReach(target) + 3 * kaiju.getScale() ? target : null;
        if (held == null) {
            kaiju.requestAbilityEnd();
            return;
        }
        if (sound != null) kaiju.level().playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), sound.get(), SoundSource.HOSTILE, 4f, 0.8f);
    }

    /** Where the victim is held, in world space. */
    private Vec3 holdPoint(KaijuEntity kaiju, LivingEntity victim) {
        Vec3 forward = Vec3.directionFromRotation(0, kaiju.yBodyRot);
        return switch (mode) {
            case FRONT -> kaiju.position().add(forward.scale(kaiju.getBbWidth() * 0.6 + victim.getBbWidth() * 0.5)).add(0, kaiju.getBbHeight() * 0.3, 0);
            case BODY -> kaiju.position().add(forward.scale(kaiju.getBbWidth() * 0.2)).add(0, kaiju.getBbHeight() * 0.2, 0);
            case MOUTH -> kaiju.getBeamOrigin(0, 1f).subtract(0, victim.getBbHeight() * 0.6, 0);
        };
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (held == null || !held.isAlive() || held.isRemoved() || held.level() != kaiju.level()) {
            kaiju.requestAbilityEnd();
            return;
        }
        Vec3 point = holdPoint(kaiju, held);
        Vec3 pull = point.subtract(held.position());
        double strength = held instanceof KaijuEntity big ? Math.min(0.6, kaiju.getBbHeight() / Math.max(1.0, big.getBbHeight()) * 0.4) : 0.6;
        held.setDeltaMovement(pull.scale(strength));
        held.hurtMarked = true;
        held.fallDistance = 0;
        if (tick % interval == 0) {
            float damage = (float) kaiju.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;
            DamageSource source = damageType != null ? ModDamageTypes.source(kaiju.level(), damageType, kaiju) : kaiju.damageSources().mobAttack(kaiju);
            com.reiko45.godzilla.util.KaijuDestruction.hurtIgnoringCooldown(held, source, damage);
            if (healFraction > 0) kaiju.heal(damage * healFraction);
            if (onHit != null) onHit.accept(kaiju, held);
        }
    }

    @Override
    public void stop(KaijuEntity kaiju) {
        if (held != null && held.isAlive() && onRelease != null) onRelease.accept(kaiju, held);
        held = null;
    }
}

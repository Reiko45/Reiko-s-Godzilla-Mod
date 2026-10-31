package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * A charged beam (atomic breath, gravity beams, Space Beam). The beam sweeps toward its target rather than snapping,
 * so players can outrun it sideways. Each emitter can track its own target (King Ghidorah's three heads).
 */
public class BeamAbility extends KaijuAbility {
    public final BeamStyle style;
    public final int emitters;
    /** Beam radius in blocks at scale 1. */
    public final float width;
    private final ResourceKey<DamageType> damageType;
    private float damage = 10;
    private int hitInterval = 4;
    private float blockRadius = 1.5f;
    private boolean setsFire;
    private float trackSpeed = 0.12f;
    private float lift;
    private float jitter;
    private boolean multiTarget;
    @Nullable
    private Supplier<SoundEvent> chargeSound;
    @Nullable
    private Supplier<SoundEvent> fireSound;
    private ParticleOptions chargeParticle = ParticleTypes.ELECTRIC_SPARK;

    private final Vec3[] aim = new Vec3[3];
    private final LivingEntity[] targets = new LivingEntity[3];

    public BeamAbility(String name, BeamStyle style, int emitters, float width, ResourceKey<DamageType> damageType,
                       int chargeTicks, int activeTicks, int cooldownTicks) {
        super(name, chargeTicks, activeTicks, cooldownTicks, "charge", "beam");
        this.style = style;
        this.emitters = Math.max(1, Math.min(3, emitters));
        this.width = width;
        this.damageType = damageType;
        this.maxRange = 48;
        this.minRange = 4;
        this.useChance = 0.03f;
    }

    public BeamAbility damage(float damage, int hitInterval) {
        this.damage = damage;
        this.hitInterval = hitInterval;
        return this;
    }

    public BeamAbility blocks(float radius, boolean fire) {
        this.blockRadius = radius;
        this.setsFire = fire;
        return this;
    }

    public BeamAbility tracking(float speed, float jitter) {
        this.trackSpeed = speed;
        this.jitter = jitter;
        return this;
    }

    /** Gravity beams toss what they hit upward. */
    public BeamAbility lift(float lift) {
        this.lift = lift;
        return this;
    }

    /** Each emitter picks a different nearby enemy when it can. */
    public BeamAbility multiTarget() {
        this.multiTarget = true;
        return this;
    }

    public BeamAbility sounds(Supplier<SoundEvent> charge, Supplier<SoundEvent> fire) {
        this.chargeSound = charge;
        this.fireSound = fire;
        return this;
    }

    public BeamAbility chargeParticle(ParticleOptions particle) {
        this.chargeParticle = particle;
        return this;
    }

    @Override
    protected boolean extraStartCheck(KaijuEntity kaiju, LivingEntity target) {
        return kaiju.hasLineOfSight(target) || kaiju.distanceTo(target) < 24 * kaiju.getScale();
    }

    @Override
    public void start(KaijuEntity kaiju, @Nullable LivingEntity target) {
        for (int i = 0; i < 3; i++) targets[i] = target;
        if (multiTarget && target != null) {
            List<LivingEntity> others = kaiju.level().getEntitiesOfClass(LivingEntity.class,
                    target.getBoundingBox().inflate(32), e -> e != target && e != kaiju && kaiju.wantsToAttack(e));
            for (int i = 1; i < emitters && i - 1 < others.size(); i++) targets[i] = others.get(i - 1);
        }
        if (chargeSound != null) {
            kaiju.level().playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), chargeSound.get(), SoundSource.HOSTILE,
                    4.0f + kaiju.getScale(), 1.0f);
        }
    }

    @Override
    public void tickCharge(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (target != null) {
            kaiju.getLookControl().setLookAt(target, 10f, 10f);
            faceTowards(kaiju, target.getX(), target.getZ(), 6f);
        }
        if (kaiju.level() instanceof ServerLevel level && tick % 2 == 0) {
            // Glow running up the spine toward the mouth: the telegraph.
            float progress = (float) tick / Math.max(1, chargeTicks);
            Vec3 mouth = kaiju.getBeamOrigin(0, 1f);
            Vec3 tail = kaiju.position().add(Vec3.directionFromRotation(0, kaiju.yBodyRot).scale(-kaiju.getBbWidth()))
                    .add(0, kaiju.getBbHeight() * 0.3, 0);
            Vec3 p = tail.lerp(mouth, progress);
            level.sendParticles(chargeParticle, p.x, p.y, p.z, 6, 0.4 * kaiju.getScale(), 0.4 * kaiju.getScale(), 0.4 * kaiju.getScale(), 0.02);
            level.sendParticles(new DustParticleOptions(new Vector3f(style.r, style.g, style.b), 2.0f),
                    mouth.x, mouth.y, mouth.z, 3, 0.3, 0.3, 0.3, 0.0);
        }
    }

    @Override
    public void activate(KaijuEntity kaiju, @Nullable LivingEntity target) {
        Vec3 forward = Vec3.directionFromRotation(kaiju.getXRot(), kaiju.yBodyRot);
        for (int i = 0; i < emitters; i++) {
            Vec3 origin = kaiju.getBeamOrigin(i, 1f);
            aim[i] = origin.add(forward.scale(10));
            kaiju.setBeamEnd(i, aim[i]);
        }
        if (fireSound != null) {
            kaiju.level().playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), fireSound.get(), SoundSource.HOSTILE,
                    5.0f + kaiju.getScale(), 1.0f);
        }
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (!(kaiju.level() instanceof ServerLevel level)) return;
        if (target != null) {
            kaiju.getLookControl().setLookAt(target, 10f, 10f);
            faceTowards(kaiju, target.getX(), target.getZ(), 3f);
        }
        double range = Math.min(128, maxRange * 1.4 * Math.max(1.0, kaiju.getScale()));
        boolean hitTick = tick % hitInterval == 0;
        boolean griefing = hitTick && GodzillaConfig.get(GodzillaConfig.BEAM_BLOCK_DAMAGE, true) && KaijuDestruction.canGrief(level, kaiju);
        int budget = GodzillaConfig.get(GodzillaConfig.MAX_BLOCKS_PER_TICK, 48);

        for (int i = 0; i < emitters; i++) {
            LivingEntity t = targets[i] != null && targets[i].isAlive() ? targets[i] : target;
            Vec3 origin = kaiju.getBeamOrigin(i, 1f);
            if (aim[i] == null) aim[i] = origin.add(kaiju.getLookAngle().scale(10));
            if (t != null) {
                Vec3 desired = t.getBoundingBox().getCenter();
                if (jitter > 0) {
                    desired = desired.add((kaiju.getRandom().nextFloat() - 0.5f) * jitter, (kaiju.getRandom().nextFloat() - 0.5f) * jitter,
                            (kaiju.getRandom().nextFloat() - 0.5f) * jitter);
                }
                aim[i] = aim[i].lerp(desired, trackSpeed);
            }
            Vec3 dir = aim[i].subtract(origin);
            if (dir.lengthSqr() < 1.0E-4) dir = kaiju.getLookAngle();
            dir = dir.normalize();
            Vec3 far = origin.add(dir.scale(range));
            BlockHitResult hit = level.clip(new ClipContext(origin, far, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, kaiju));
            Vec3 end = hit.getType() == HitResult.Type.MISS ? far : hit.getLocation();
            kaiju.setBeamEnd(i, end);

            if (hitTick) {
                float radius = width * kaiju.getScale() + 0.4f;
                KaijuDestruction.damageAlongSegment(kaiju, origin, end, radius,
                        com.reiko45.godzilla.registry.ModDamageTypes.source(level, damageType, kaiju), damage,
                        e -> shouldHit(kaiju, e, t),
                        e -> {
                            // Toss small things into the air; something as heavy as a kaiju only staggers.
                            if (lift > 0 && !(e instanceof KaijuEntity)) e.push(0, lift, 0);
                            if (setsFire && !e.fireImmune()) e.igniteForSeconds(4);
                        });
                if (hit.getType() != HitResult.Type.MISS) {
                    float r = blockRadius * Math.max(1.0f, kaiju.getScale() * 0.75f);
                    if (griefing) KaijuDestruction.breakSphere(level, end, r, budget / emitters, setsFire, kaiju.getRandom());
                    level.sendParticles(ParticleTypes.EXPLOSION, end.x, end.y, end.z, 2, r * 0.5, r * 0.5, r * 0.5, 0.0);
                }
            }
            if (tick % 3 == 0) {
                for (int s = 0; s < 6; s++) {
                    Vec3 p = origin.lerp(end, kaiju.getRandom().nextDouble());
                    level.sendParticles(new DustParticleOptions(new Vector3f(style.r, style.g, style.b), 2.5f), p.x, p.y, p.z, 1, 0.2, 0.2, 0.2, 0);
                }
                level.sendParticles(ParticleTypes.LARGE_SMOKE, end.x, end.y, end.z, 3, 0.5, 0.5, 0.5, 0.02);
            }
        }
    }

    private static boolean shouldHit(KaijuEntity kaiju, LivingEntity e, @Nullable LivingEntity target) {
        if (e == target) return true;
        if (kaiju.isAlliedTo(e) || kaiju.hasPassenger(e)) return false;
        if (e instanceof Player p && (p.isCreative() || p.isSpectator())) return false;
        // Don't fry friendly kaiju standing in the way.
        if (e instanceof com.reiko45.godzilla.entity.KaijuAligned other && !kaiju.getFaction().isHostileTo(other.getFaction())) return false;
        return true;
    }

    @Override
    public void stop(KaijuEntity kaiju) {
        for (int i = 0; i < 3; i++) {
            targets[i] = null;
            aim[i] = null;
        }
    }
}

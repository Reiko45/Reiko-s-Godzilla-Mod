package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Digs underground, tunnels under the target (a trail of churned earth shows where) and bursts out beneath it,
 * flinging everything up: Megalon's Double Drill, Baragon, Kumonga's ambush. Nothing can touch the kaiju while it's
 * under the ground.
 */
public class BurrowAbility extends KaijuAbility {
    private final double speed;
    private final float damageMultiplier;
    @Nullable
    private Vec3 goal;

    public BurrowAbility(String name, double speed, float damageMultiplier, int cooldown) {
        super(name, 24, 120, cooldown, "dig", "burrow");
        this.speed = speed;
        this.damageMultiplier = damageMultiplier;
        this.minRange = 10;
        this.maxRange = 64;
        this.useChance = 0.02f;
    }

    public static boolean isBurrowed(KaijuEntity kaiju) {
        return kaiju.getActiveAbility() instanceof BurrowAbility && kaiju.getAbilityPhase() == KaijuEntity.PHASE_ACTIVE;
    }

    @Override
    protected boolean extraStartCheck(KaijuEntity kaiju, LivingEntity target) {
        return kaiju.onGround() && !kaiju.isInWater();
    }

    @Override
    public void tickCharge(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (kaiju.level() instanceof ServerLevel level && tick % 2 == 0) {
            dirt(level, kaiju.position(), kaiju.getBbWidth() * 0.6, 20);
            if (tick % 8 == 0) level.playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), SoundEvents.GRAVEL_BREAK, SoundSource.HOSTILE, 4f, 0.5f);
        }
    }

    @Override
    public void activate(KaijuEntity kaiju, @Nullable LivingEntity target) {
        goal = target != null ? target.position() : kaiju.position().add(Vec3.directionFromRotation(0, kaiju.yBodyRot).scale(24));
        kaiju.setInvisible(true);
        kaiju.setInvulnerable(true);
        kaiju.noPhysics = true;
        kaiju.level().playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), SoundEvents.ROOTED_DIRT_BREAK, SoundSource.HOSTILE, 5f, 0.4f);
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (!(kaiju.level() instanceof ServerLevel level)) return;
        if (target != null && target.isAlive()) goal = target.position();
        if (goal == null) {
            kaiju.requestAbilityEnd();
            return;
        }
        Vec3 flat = goal.subtract(kaiju.position()).multiply(1, 0, 1);
        double step = speed * Math.max(1.0, Math.sqrt(kaiju.getScale()));
        double reach = 1.5 + kaiju.getBbWidth() * 0.4;
        if (flat.length() <= reach || tick >= activeTicks - 2) {
            erupt(level, kaiju);
            kaiju.requestAbilityEnd();
            return;
        }
        Vec3 move = flat.normalize().scale(Math.min(step, flat.length()));
        double nx = kaiju.getX() + move.x, nz = kaiju.getZ() + move.z;
        int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) Math.floor(nx), (int) Math.floor(nz));
        kaiju.setPos(nx, surface, nz);
        kaiju.setDeltaMovement(Vec3.ZERO);
        float yaw = (float) Math.toDegrees(Math.atan2(move.z, move.x)) - 90f;
        kaiju.setYRot(yaw);
        kaiju.yBodyRot = yaw;
        dirt(level, kaiju.position(), kaiju.getBbWidth() * 0.4, 8);
        if (tick % 6 == 0) level.playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), SoundEvents.GRAVEL_BREAK, SoundSource.HOSTILE, 3f, 0.4f);
    }

    private void erupt(ServerLevel level, KaijuEntity kaiju) {
        surface(kaiju);
        double radius = 4 * kaiju.getScale() + kaiju.getBbWidth() * 0.5;
        float damage = (float) kaiju.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;
        level.playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 5f, 0.5f);
        dirt(level, kaiju.position(), radius, 150);
        level.sendParticles(ParticleTypes.EXPLOSION, kaiju.getX(), kaiju.getY() + 1, kaiju.getZ(), 6, radius * 0.3, 1, radius * 0.3, 0);
        for (LivingEntity e : AreaAbility.victims(kaiju, radius)) {
            if (e.hurt(kaiju.damageSources().mobAttack(kaiju), damage)) {
                e.push(0, 1.1, 0);
                e.hurtMarked = true;
            }
        }
        if (KaijuDestruction.canGrief(level, kaiju)) {
            KaijuDestruction.breakSphere(level, kaiju.position(), (float) Math.min(5, 1.2 * kaiju.getScale() + 1),
                    GodzillaConfig.get(GodzillaConfig.MAX_BLOCKS_PER_TICK, 48), false, kaiju.getRandom());
        }
        kaiju.setDeltaMovement(0, 0.6, 0);
    }

    private static void surface(KaijuEntity kaiju) {
        kaiju.setInvisible(false);
        kaiju.setInvulnerable(false);
        kaiju.noPhysics = false;
    }

    private static void dirt(ServerLevel level, Vec3 at, double spread, int count) {
        BlockPos below = BlockPos.containing(at).below();
        BlockState state = level.getBlockState(below);
        if (state.isAir() || !state.getFluidState().isEmpty()) state = Blocks.DIRT.defaultBlockState();
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), at.x, at.y + 0.3, at.z, count, spread, 0.4, spread, 0.15);
    }

    @Override
    public void stop(KaijuEntity kaiju) {
        if (kaiju.isInvisible()) surface(kaiju);
        goal = null;
    }
}

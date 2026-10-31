package com.reiko45.godzilla.entity.ai;

import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** Goals used by flying kaiju: taking off, circling a target, landing and drifting around. */
public final class FlyingGoals {
    private FlyingGoals() {}

    static int groundY(FlyingKaijuEntity k, double x, double z) {
        return k.level().getHeight(Heightmap.Types.MOTION_BLOCKING, (int) Math.floor(x), (int) Math.floor(z));
    }

    public static class TakeOff extends Goal {
        private final FlyingKaijuEntity k;

        public TakeOff(FlyingKaijuEntity k) {
            this.k = k;
            setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (k.isFlying() || k.isVehicle() || k.isAbilityActive() || !k.canFlyNow()) return false;
            LivingEntity t = k.getTarget();
            if (t != null) return k.prefersFlightInCombat() || t.getY() > k.getY() + k.getBbHeight() || k.distanceToSqr(t) > 40 * 40;
            return k.getRandom().nextInt(500) == 0;
        }

        @Override
        public void start() {
            k.setFlying(true);
            k.setDeltaMovement(k.getDeltaMovement().add(0, 0.9, 0));
            k.playWingSound();
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }

    /** Circle above the target, staying in range for beams and swooping in to strike. */
    public static class Combat extends Goal {
        private final FlyingKaijuEntity k;
        private double angle;
        private int direction = 1;

        public Combat(FlyingKaijuEntity k) {
            this.k = k;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity t = k.getTarget();
            return k.isFlying() && t != null && t.isAlive() && !k.isVehicle();
        }

        @Override
        public void start() {
            LivingEntity t = k.getTarget();
            if (t != null) angle = Math.atan2(k.getZ() - t.getZ(), k.getX() - t.getX());
            direction = k.getRandom().nextBoolean() ? 1 : -1;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity t = k.getTarget();
            if (t == null) return;
            k.getLookControl().setLookAt(t, 20f, 20f);
            double reach = k.getMeleeReach(t);
            boolean swooping = k.meleeCooldown <= 0 && k.getRandom().nextInt(3) != 0 && k.distanceToSqr(t) < Math.pow(reach * 4, 2);
            if (swooping) {
                k.getMoveControl().setWantedPosition(t.getX(), t.getY() + t.getBbHeight() * 0.5, t.getZ(), 1.4);
                if (k.distanceToSqr(t) < reach * reach) k.performMelee(t);
                return;
            }
            angle += 0.025 * direction;
            if (k.getRandom().nextInt(400) == 0) direction = -direction;
            double radius = 12 + k.getBbWidth() * 2.0;
            double x = t.getX() + Math.cos(angle) * radius;
            double z = t.getZ() + Math.sin(angle) * radius;
            double y = t.getY() + 8 + k.getBbHeight();
            y = Math.max(y, groundY(k, x, z) + 6 + k.getBbHeight() * 0.5);
            k.getMoveControl().setWantedPosition(x, y, z, 1.0);
        }
    }

    public static class Land extends Goal {
        private final FlyingKaijuEntity k;
        private Vec3 spot;
        private int timer;

        public Land(FlyingKaijuEntity k) {
            this.k = k;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return k.isFlying() && !k.alwaysFlies() && k.getTarget() == null && !k.isVehicle() && k.getRandom().nextInt(k.landChance()) == 0;
        }

        @Override
        public void start() {
            timer = 0;
            spot = new Vec3(k.getX(), groundY(k, k.getX(), k.getZ()), k.getZ());
        }

        @Override
        public boolean canContinueToUse() {
            return k.isFlying() && k.getTarget() == null && timer < 400;
        }

        @Override
        public void tick() {
            timer++;
            k.getMoveControl().setWantedPosition(spot.x, spot.y, spot.z, 0.6);
            if (k.onGround() || k.getY() - spot.y < 1.0) k.setFlying(false);
        }
    }

    public static class Wander extends Goal {
        private final FlyingKaijuEntity k;
        private Vec3 goal;
        private int timer;

        public Wander(FlyingKaijuEntity k) {
            this.k = k;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!k.isFlying() || k.getTarget() != null || k.isVehicle() || k.getRandom().nextInt(40) != 0) return false;
            BlockPos dest = k.getDestination();
            double x, z;
            if (dest != null && dest.distToCenterSqr(k.position()) > 32 * 32) {
                x = dest.getX();
                z = dest.getZ();
            } else {
                double a = k.getRandom().nextDouble() * Math.PI * 2;
                double d = 16 + k.getRandom().nextDouble() * 40;
                x = k.getX() + Math.cos(a) * d;
                z = k.getZ() + Math.sin(a) * d;
            }
            if (!k.level().hasChunkAt(BlockPos.containing(x, 0, z))) return false;
            double y = groundY(k, x, z) + 12 + k.getRandom().nextDouble() * 18 + k.getBbHeight();
            goal = new Vec3(x, y, z);
            return true;
        }

        @Override
        public void start() {
            timer = 0;
        }

        @Override
        public boolean canContinueToUse() {
            return k.isFlying() && k.getTarget() == null && timer < 300 && goal.distanceToSqr(k.position()) > 9;
        }

        @Override
        public void tick() {
            timer++;
            k.getMoveControl().setWantedPosition(goal.x, goal.y, goal.z, 0.7);
        }
    }
}

package com.reiko45.godzilla.entity.ai;

import com.reiko45.godzilla.entity.TameableKaijuEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;

/** Goals for tamed kaiju: sit, follow, and fight alongside the owner. */
public final class OwnerGoals {
    private OwnerGoals() {}

    public static class Sit extends Goal {
        private final TameableKaijuEntity a;

        public Sit(TameableKaijuEntity a) {
            this.a = a;
            setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return a.isTame() && a.isSitting() && !a.isVehicle() && !a.isAbilityActive();
        }

        @Override
        public void start() {
            a.getNavigation().stop();
            a.setTarget(null);
        }
    }

    public static class FollowOwner extends Goal {
        private final TameableKaijuEntity a;
        private LivingEntity owner;

        public FollowOwner(TameableKaijuEntity a) {
            this.a = a;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!a.isTame() || a.isSitting() || a.isVehicle() || a.getTarget() != null) return false;
            owner = a.getOwner();
            return owner != null && !owner.isSpectator() && a.distanceToSqr(owner) > Math.pow(10 + a.getBbWidth(), 2);
        }

        @Override
        public boolean canContinueToUse() {
            return owner != null && owner.isAlive() && !a.isSitting() && a.getTarget() == null
                    && a.distanceToSqr(owner) > Math.pow(5 + a.getBbWidth(), 2);
        }

        @Override
        public void tick() {
            a.getLookControl().setLookAt(owner, 10f, a.getMaxHeadXRot());
            double speed = a.distanceToSqr(owner) > 30 * 30 ? 1.4 : 1.0;
            a.getMoveControl().setWantedPosition(owner.getX(), owner.getY(), owner.getZ(), speed);
        }
    }

    /** Attack whatever hurt the owner. */
    public static class DefendOwner extends TargetGoal {
        private final TameableKaijuEntity a;
        private LivingEntity attacker;
        private int timestamp;

        public DefendOwner(TameableKaijuEntity a) {
            super(a, false);
            this.a = a;
        }

        @Override
        public boolean canUse() {
            if (!a.isTame() || a.isSitting()) return false;
            LivingEntity owner = a.getOwner();
            if (owner == null) return false;
            attacker = owner.getLastHurtByMob();
            return attacker != null && owner.getLastHurtByMobTimestamp() != timestamp
                    && canAttack(attacker, TargetingConditions.DEFAULT) && !a.isAlliedTo(attacker);
        }

        @Override
        public void start() {
            a.setTarget(attacker);
            LivingEntity owner = a.getOwner();
            if (owner != null) timestamp = owner.getLastHurtByMobTimestamp();
            super.start();
        }
    }

    /** Attack whatever the owner attacks. */
    public static class AssistOwner extends TargetGoal {
        private final TameableKaijuEntity a;
        private LivingEntity victim;
        private int timestamp;

        public AssistOwner(TameableKaijuEntity a) {
            super(a, false);
            this.a = a;
        }

        @Override
        public boolean canUse() {
            if (!a.isTame() || a.isSitting()) return false;
            LivingEntity owner = a.getOwner();
            if (owner == null) return false;
            victim = owner.getLastHurtMob();
            return victim != null && owner.getLastHurtMobTimestamp() != timestamp
                    && canAttack(victim, TargetingConditions.DEFAULT) && !a.isAlliedTo(victim);
        }

        @Override
        public void start() {
            a.setTarget(victim);
            LivingEntity owner = a.getOwner();
            if (owner != null) timestamp = owner.getLastHurtMobTimestamp();
            super.start();
        }
    }
}

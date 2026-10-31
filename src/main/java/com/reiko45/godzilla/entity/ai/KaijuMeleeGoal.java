package com.reiko45.godzilla.entity.ai;

import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** Walk up to the target in a straight line and strike when in reach. */
public class KaijuMeleeGoal extends Goal {
    private final KaijuEntity kaiju;

    public KaijuMeleeGoal(KaijuEntity kaiju) {
        this.kaiju = kaiju;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean grounded() {
        return !(kaiju instanceof FlyingKaijuEntity f && f.isFlying());
    }

    @Override
    public boolean canUse() {
        LivingEntity t = kaiju.getTarget();
        return t != null && t.isAlive() && grounded() && kaiju.getControllingPassenger() == null;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity t = kaiju.getTarget();
        if (t == null) return;
        kaiju.getLookControl().setLookAt(t, 30f, 30f);
        double reach = kaiju.getMeleeReach(t);
        double dx = t.getX() - kaiju.getX();
        double dz = t.getZ() - kaiju.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        double vertical = t.getY() - kaiju.getY();
        if (horizontal > reach * 0.85) {
            kaiju.getMoveControl().setWantedPosition(t.getX(), t.getY(), t.getZ(), 1.0);
        }
        if (horizontal <= reach && vertical < kaiju.getBbHeight() + 2 && vertical > -6 && kaiju.meleeCooldown <= 0) {
            kaiju.performMelee(t);
        }
    }
}

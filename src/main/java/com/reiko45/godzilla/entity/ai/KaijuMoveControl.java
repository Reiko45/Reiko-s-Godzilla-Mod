package com.reiko45.godzilla.entity.ai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;

/**
 * Walks straight toward the wanted position (no pathfinding: kaiju are too big for it, and they smash through
 * whatever is in the way). Turns slowly so huge bodies feel heavy.
 */
public class KaijuMoveControl extends MoveControl {
    protected float turnSpeed = 7.0f;

    public KaijuMoveControl(Mob mob) {
        super(mob);
    }

    @Override
    public void tick() {
        if (this.operation != Operation.MOVE_TO) {
            this.mob.setZza(0);
            return;
        }
        this.operation = Operation.WAIT;
        double dx = this.wantedX - this.mob.getX();
        double dz = this.wantedZ - this.mob.getZ();
        double distSq = dx * dx + dz * dz;
        double stop = Math.max(0.5, this.mob.getBbWidth() * 0.25);
        if (distSq < stop * stop) {
            this.mob.setZza(0);
            return;
        }
        float targetYaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        this.mob.setYRot(this.rotlerp(this.mob.getYRot(), targetYaw, turnSpeed));
        float speed = (float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
        if (Mth.degreesDifferenceAbs(this.mob.getYRot(), targetYaw) > 60) speed *= 0.3f;
        this.mob.setSpeed(speed);
    }
}

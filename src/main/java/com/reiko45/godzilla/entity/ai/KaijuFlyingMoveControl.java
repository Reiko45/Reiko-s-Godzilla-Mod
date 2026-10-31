package com.reiko45.godzilla.entity.ai;

import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

/** Smooth 3D flight with banking turns. Falls back to walking when the kaiju has landed. */
public class KaijuFlyingMoveControl extends KaijuMoveControl {
    private final FlyingKaijuEntity flyer;

    public KaijuFlyingMoveControl(FlyingKaijuEntity flyer) {
        super(flyer);
        this.flyer = flyer;
    }

    @Override
    public void tick() {
        if (!flyer.isFlying()) {
            super.tick();
            return;
        }
        flyer.setZza(0);
        if (this.operation == Operation.MOVE_TO) {
            this.operation = Operation.WAIT;
            Vec3 delta = new Vec3(wantedX - flyer.getX(), wantedY - flyer.getY(), wantedZ - flyer.getZ());
            double dist = delta.length();
            if (dist < 1.0) {
                flyer.setDeltaMovement(flyer.getDeltaMovement().scale(0.6));
                return;
            }
            double maxSpeed = speedModifier * flyer.getAttributeValue(Attributes.FLYING_SPEED) * Math.max(1.0, Math.sqrt(flyer.getScale()));
            Vec3 desired = delta.scale(Math.min(maxSpeed, dist * 0.25) / dist);
            Vec3 v = flyer.getDeltaMovement();
            flyer.setDeltaMovement(v.add(desired.subtract(v).scale(0.12)));
            float yaw = (float) (Mth.atan2(delta.z, delta.x) * (180.0 / Math.PI)) - 90.0f;
            flyer.setYRot(Mth.approachDegrees(flyer.getYRot(), yaw, 6.0f));
            flyer.yBodyRot = flyer.getYRot();
        } else {
            Vec3 v = flyer.getDeltaMovement().scale(0.85);
            flyer.setDeltaMovement(v.x, v.y + Math.sin(flyer.tickCount * 0.08) * 0.01, v.z);
        }
    }
}

package com.reiko45.godzilla.client;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.VehicleWeapon;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import javax.annotation.Nullable;

/**
 * Third-person camera while piloting: shifted to the right of the machine and turned so the crosshair sits on the
 * exact point the weapons will hit (the server aims along the pilot's own line of sight).
 */
public final class PilotCamera {
    /** Same reach as {@link KaijuEntity#getPilotAimPoint()}. */
    private static final double AIM_RANGE = 160;

    public record Result(Vec3 position, float yRot, float xRot) {}

    private PilotCamera() {}

    @Nullable
    public static Result adjust(BlockGetter level, Entity entity, Vec3 cameraPos, float partialTick) {
        if (!(entity instanceof Player player) || !(player.getVehicle() instanceof KaijuEntity kaiju)
                || !(kaiju instanceof VehicleWeapon) || kaiju.getControllingPassenger() != player) {
            return null;
        }
        Vec3 eye = player.getEyePosition(partialTick);
        Vec3 look = player.getViewVector(partialTick);
        Vec3 far = eye.add(look.scale(AIM_RANGE));
        BlockHitResult aimHit = level.clip(new ClipContext(eye, far, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
        Vec3 aim = aimHit.getType() == HitResult.Type.MISS ? far : aimHit.getLocation();

        // Slide right (screen-space) by a bit more than half the machine's width, stopping short of walls.
        float yaw = player.getViewYRot(partialTick) * Mth.DEG_TO_RAD;
        Vec3 right = new Vec3(-Mth.cos(yaw), 0, -Mth.sin(yaw));
        double shift = kaiju.getBbWidth() * 0.6 + 1.5;
        Vec3 wanted = cameraPos.add(right.scale(shift)).add(0, kaiju.getBbHeight() * 0.05, 0);
        BlockHitResult wall = level.clip(new ClipContext(cameraPos, wanted, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty()));
        Vec3 position = wall.getType() == HitResult.Type.MISS ? wanted : cameraPos.lerp(wall.getLocation(), 0.85);

        // Converge on the aim point so the crosshair shows where shots land.
        Vec3 d = aim.subtract(position);
        double horizontal = Math.sqrt(d.x * d.x + d.z * d.z);
        if (horizontal < 1.0E-3) return new Result(position, player.getViewYRot(partialTick), player.getViewXRot(partialTick));
        float yRot = (float) (Mth.atan2(d.z, d.x) * Mth.RAD_TO_DEG) - 90.0f;
        float xRot = (float) -(Mth.atan2(d.y, horizontal) * Mth.RAD_TO_DEG);
        return new Result(position, yRot, xRot);
    }
}

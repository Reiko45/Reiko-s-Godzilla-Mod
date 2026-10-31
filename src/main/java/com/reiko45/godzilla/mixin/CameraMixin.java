package com.reiko45.godzilla.mixin;

import com.reiko45.godzilla.client.PilotCamera;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Over-the-shoulder camera for mech and kaiju pilots. Without it the camera sits straight behind the machine, so any
 * beam fired where you look is hidden behind the machine's own body.
 */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void setPosition(Vec3 position);

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Shadow
    public abstract Vec3 getPosition();

    @Inject(method = "setup", at = @At("TAIL"))
    private void reikogodzilla$pilotCamera(BlockGetter level, Entity entity, boolean detached, boolean mirrored, float partialTick,
                                          CallbackInfo ci) {
        if (!detached || mirrored) return;
        PilotCamera.Result result = PilotCamera.adjust(level, entity, getPosition(), partialTick);
        if (result != null) {
            setPosition(result.position());
            setRotation(result.yRot(), result.xRot());
        }
    }
}

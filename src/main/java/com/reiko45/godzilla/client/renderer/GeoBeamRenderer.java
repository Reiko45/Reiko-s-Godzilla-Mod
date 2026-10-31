package com.reiko45.godzilla.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.reiko45.godzilla.client.BeamRenderer;
import com.reiko45.godzilla.entity.BeamEmitter;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Mob;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** GeckoLib renderer for non-kaiju entities that may draw beams (tanks, SpaceGodzilla's crystals). */
public class GeoBeamRenderer<T extends Mob & GeoEntity> extends GeoEntityRenderer<T> {
    public GeoBeamRenderer(EntityRendererProvider.Context context, GeoModel<T> model, float shadow) {
        super(context, model);
        this.shadowRadius = shadow;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
        if (entity instanceof BeamEmitter) renderBeams(entity, partialTick, poseStack, buffers);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void renderBeams(Mob entity, float partialTick, PoseStack poseStack, MultiBufferSource buffers) {
        BeamRenderer.renderBeams((Mob & BeamEmitter) (Object) entity, partialTick, poseStack, buffers);
    }
}

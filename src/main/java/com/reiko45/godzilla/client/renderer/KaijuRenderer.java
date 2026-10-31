package com.reiko45.godzilla.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.reiko45.godzilla.client.BeamRenderer;
import com.reiko45.godzilla.entity.KaijuEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** GeckoLib renderer for every kaiju, plus their beams. */
public class KaijuRenderer<T extends KaijuEntity> extends GeoEntityRenderer<T> {
    public KaijuRenderer(EntityRendererProvider.Context context, GeoModel<T> model, float shadow) {
        super(context, model);
        this.shadowRadius = shadow;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
        BeamRenderer.renderBeams(entity, partialTick, poseStack, buffers);
    }

    @Override
    protected float getDeathMaxRotation(T entity) {
        return 90f;
    }
}

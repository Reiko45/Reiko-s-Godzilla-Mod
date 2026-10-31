package com.reiko45.godzilla.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.reiko45.godzilla.ReikoGodzilla;
import com.reiko45.godzilla.client.BeamRenderer;
import com.reiko45.godzilla.entity.vehicle.MaserTankEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MaserTankRenderer extends GeoEntityRenderer<MaserTankEntity> {
    public MaserTankRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(ReikoGodzilla.id("maser_tank")));
        this.shadowRadius = 1.4f;
    }

    @Override
    public void render(MaserTankEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
        BeamRenderer.renderBeams(entity, partialTick, poseStack, buffers);
    }
}

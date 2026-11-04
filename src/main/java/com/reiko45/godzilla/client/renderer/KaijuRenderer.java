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
    private java.util.function.Predicate<T> glowWhen = e -> true;

    public KaijuRenderer(EntityRendererProvider.Context context, GeoModel<T> model, float shadow) {
        super(context, model);
        this.shadowRadius = shadow;
        addRenderLayer(new GlowLayer<>(this, e -> glowWhen.test(e)));
    }

    /** Only light up the {@code _glow} overlay while this holds (by default it is always on when the file exists). */
    public KaijuRenderer<T> glowWhen(java.util.function.Predicate<T> condition) {
        this.glowWhen = condition;
        return this;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
        BeamRenderer.renderBeams(entity, partialTick, poseStack, buffers);
    }

    /** Icy blue while freezing, orange while Burning Godzilla melts down, and so on. */
    @Override
    public software.bernie.geckolib.util.Color getRenderColor(T entity, float partialTick, int packedLight) {
        float[] tint = entity.getTint();
        if (tint == null) return super.getRenderColor(entity, partialTick, packedLight);
        return software.bernie.geckolib.util.Color.ofRGBA(tint[0], tint[1], tint[2], 1.0f);
    }

    @Override
    protected float getDeathMaxRotation(T entity) {
        return 90f;
    }
}

package com.reiko45.godzilla.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Draws {@code <texture>_glow.png} over the model at full brightness, if that file exists.
 * Transparent pixels are skipped, so the overlay only needs the glowing parts: eyes, dorsal plates, maser coils.
 */
public class GlowLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {
    private static final Map<ResourceLocation, Boolean> EXISTS = new ConcurrentHashMap<>();
    private final Predicate<T> active;

    public GlowLayer(GeoRenderer<T> renderer, Predicate<T> active) {
        super(renderer);
        this.active = active;
    }

    public static ResourceLocation glowTexture(ResourceLocation texture) {
        String path = texture.getPath();
        if (path.endsWith(".png")) path = path.substring(0, path.length() - 4);
        return ResourceLocation.fromNamespaceAndPath(texture.getNamespace(), path + "_glow.png");
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource,
                       VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (animatable instanceof Entity entity && entity.isInvisible()) return;
        if (!active.test(animatable)) return;
        ResourceLocation glow = glowTexture(getTextureResource(animatable));
        if (!EXISTS.computeIfAbsent(glow, loc -> Minecraft.getInstance().getResourceManager().getResource(loc).isPresent())) return;
        RenderType emissive = RenderType.entityTranslucentEmissive(glow);
        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, emissive, bufferSource.getBuffer(emissive), partialTick,
                LightTexture.FULL_BRIGHT, packedOverlay, 0xFFFFFFFF);
    }
}

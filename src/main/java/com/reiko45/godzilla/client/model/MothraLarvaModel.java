package com.reiko45.godzilla.client.model;

import com.reiko45.godzilla.entity.kaiju.MothraLarvaEntity;
import net.minecraft.resources.ResourceLocation;

/** Swaps to the cocoon model while the larva is pupating. */
public class MothraLarvaModel extends KaijuModel<MothraLarvaEntity> {
    public MothraLarvaModel() {
        super("mothra_larva");
    }

    private static String name(MothraLarvaEntity entity) {
        return entity.isCocoon() ? "mothra_cocoon" : "mothra_larva";
    }

    @Override
    public ResourceLocation getModelResource(MothraLarvaEntity entity) {
        return geo(name(entity));
    }

    @Override
    public ResourceLocation getTextureResource(MothraLarvaEntity entity) {
        return tex(name(entity));
    }

    @Override
    public ResourceLocation getAnimationResource(MothraLarvaEntity entity) {
        return anim(name(entity));
    }
}

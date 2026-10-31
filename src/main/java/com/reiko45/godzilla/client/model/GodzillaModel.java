package com.reiko45.godzilla.client.model;

import com.reiko45.godzilla.entity.kaiju.GodzillaEntity;
import net.minecraft.resources.ResourceLocation;

/** Each Godzilla incarnation gets its own model, animations and texture: godzilla_showa, godzilla_heisei, ... */
public class GodzillaModel extends KaijuModel<GodzillaEntity> {
    public GodzillaModel() {
        super("godzilla_showa");
    }

    private static String name(GodzillaEntity entity) {
        return entity.getVariant() == GodzillaEntity.HEISEI ? "godzilla_heisei" : "godzilla_showa";
    }

    @Override
    public ResourceLocation getModelResource(GodzillaEntity entity) {
        return geo(name(entity));
    }

    @Override
    public ResourceLocation getTextureResource(GodzillaEntity entity) {
        return tex(name(entity));
    }

    @Override
    public ResourceLocation getAnimationResource(GodzillaEntity entity) {
        return anim(name(entity));
    }
}

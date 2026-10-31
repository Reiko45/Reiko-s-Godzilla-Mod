package com.reiko45.godzilla.client.model;

import com.reiko45.godzilla.entity.kaiju.MechagodzillaEntity;
import net.minecraft.resources.ResourceLocation;

/** While disguised, Mechagodzilla wears the Showa Godzilla model and texture. */
public class MechagodzillaModel extends KaijuModel<MechagodzillaEntity> {
    public MechagodzillaModel() {
        super("mechagodzilla");
    }

    private static String name(MechagodzillaEntity entity) {
        return entity.isDisguised() ? "godzilla_showa" : "mechagodzilla";
    }

    @Override
    public ResourceLocation getModelResource(MechagodzillaEntity entity) {
        return geo(name(entity));
    }

    @Override
    public ResourceLocation getTextureResource(MechagodzillaEntity entity) {
        return tex(name(entity));
    }

    @Override
    public ResourceLocation getAnimationResource(MechagodzillaEntity entity) {
        return anim(name(entity));
    }
}

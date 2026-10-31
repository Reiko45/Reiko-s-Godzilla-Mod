package com.reiko45.godzilla.client.model;

import com.reiko45.godzilla.ReikoGodzilla;
import com.reiko45.godzilla.entity.KaijuEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

/**
 * Loads geo/entity/&lt;name&gt;.geo.json, animations/entity/&lt;name&gt;.animation.json and textures/entity/&lt;name&gt;.png.
 * A bone called "head" turns to follow where the kaiju is looking.
 */
public class KaijuModel<T extends KaijuEntity> extends DefaultedEntityGeoModel<T> {
    public KaijuModel(String name) {
        super(ReikoGodzilla.id(name), true);
    }

    protected static ResourceLocation geo(String name) {
        return ReikoGodzilla.id("geo/entity/" + name + ".geo.json");
    }

    protected static ResourceLocation anim(String name) {
        return ReikoGodzilla.id("animations/entity/" + name + ".animation.json");
    }

    protected static ResourceLocation tex(String name) {
        return ReikoGodzilla.id("textures/entity/" + name + ".png");
    }
}

package com.reiko45.godzilla.client.model;

import com.reiko45.godzilla.ReikoGodzilla;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

import java.util.function.Function;

/**
 * A GeckoLib model whose files depend on the entity's state (variant, form, disguise...). One function picks the
 * model + animation name, another the texture name.
 */
public class NamedGeoModel<T extends GeoAnimatable> extends DefaultedEntityGeoModel<T> {
    private final Function<T, String> model;
    private final Function<T, String> texture;

    public NamedGeoModel(Function<T, String> model, Function<T, String> texture) {
        super(ReikoGodzilla.id("placeholder"), true);
        this.model = model;
        this.texture = texture;
    }

    public NamedGeoModel(Function<T, String> name) {
        this(name, name);
    }

    public static <T extends GeoAnimatable> NamedGeoModel<T> fixed(String name) {
        return new NamedGeoModel<>(e -> name);
    }

    @Override
    public ResourceLocation getModelResource(T entity) {
        return ReikoGodzilla.id("geo/entity/" + model.apply(entity) + ".geo.json");
    }

    @Override
    public ResourceLocation getAnimationResource(T entity) {
        return ReikoGodzilla.id("animations/entity/" + model.apply(entity) + ".animation.json");
    }

    @Override
    public ResourceLocation getTextureResource(T entity) {
        return ReikoGodzilla.id("textures/entity/" + texture.apply(entity) + ".png");
    }
}

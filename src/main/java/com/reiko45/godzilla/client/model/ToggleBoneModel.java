package com.reiko45.godzilla.client.model;

import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * A {@link NamedGeoModel} that shows or hides some bones depending on the entity: Kong's axe and glove, MonsterVerse
 * Ghidorah's severed heads, the Skar King's crystal. Missing bones (in your own Blockbench models) are ignored.
 */
public class ToggleBoneModel<T extends GeoAnimatable> extends NamedGeoModel<T> {
    private final List<String> bones;
    private final BiPredicate<T, String> visible;

    public ToggleBoneModel(Function<T, String> model, Function<T, String> texture, List<String> bones, BiPredicate<T, String> visible) {
        super(model, texture);
        this.bones = bones;
        this.visible = visible;
    }

    @Override
    public void setCustomAnimations(T animatable, long instanceId, AnimationState<T> state) {
        super.setCustomAnimations(animatable, instanceId, state);
        for (String name : bones) {
            GeoBone bone = getAnimationProcessor().getBone(name);
            if (bone != null) bone.setHidden(!visible.test(animatable, name));
        }
    }
}

package com.reiko45.godzilla.registry;

import com.reiko45.godzilla.ReikoGodzilla;
import com.reiko45.godzilla.effect.FrostbiteEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, ReikoGodzilla.MODID);

    public static final DeferredHolder<MobEffect, FrostbiteEffect> FROSTBITE = EFFECTS.register("frostbite", FrostbiteEffect::new);

    public static final DeferredHolder<MobEffect, com.reiko45.godzilla.effect.RepellentEffect> REPELLENT =
            EFFECTS.register("repellent", com.reiko45.godzilla.effect.RepellentEffect::new);

    private ModEffects() {}
}

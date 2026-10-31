package com.reiko45.godzilla.registry;

import com.reiko45.godzilla.ReikoGodzilla;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Sound events. assets/reikogodzilla/sounds.json currently points these at re-pitched vanilla sounds; drop your own
 * .ogg files into assets/reikogodzilla/sounds/ and update sounds.json to replace them.
 */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, ReikoGodzilla.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> GODZILLA_ROAR = register("godzilla_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> GODZILLA_DEATH = register("godzilla_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> KAIJU_HURT = register("kaiju_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> ATOMIC_BREATH_CHARGE = register("atomic_breath_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> ATOMIC_BREATH_FIRE = register("atomic_breath_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> GHIDORAH_CALL = register("ghidorah_call");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRAVITY_BEAM_CHARGE = register("gravity_beam_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRAVITY_BEAM_FIRE = register("gravity_beam_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOTHRA_CRY = register("mothra_cry");
    public static final DeferredHolder<SoundEvent, SoundEvent> RODAN_SCREECH = register("rodan_screech");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANGUIRUS_ROAR = register("anguirus_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> MECHAGODZILLA_ROAR = register("mechagodzilla_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPACE_BEAM_CHARGE = register("space_beam_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPACE_BEAM_FIRE = register("space_beam_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> MASER_FIRE = register("maser_fire");

    private ModSounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(ReikoGodzilla.id(name)));
    }
}

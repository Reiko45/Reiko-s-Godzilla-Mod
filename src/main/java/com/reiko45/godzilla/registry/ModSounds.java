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
    // Phase 2
    public static final DeferredHolder<SoundEvent, SoundEvent> BATTRA_CRY = register("battra_cry");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPACEGODZILLA_ROAR = register("spacegodzilla_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> DESTOROYAH_ROAR = register("destoroyah_roar");

    // Phase 3
    public static final DeferredHolder<SoundEvent, SoundEvent> MONSTERVERSE_GODZILLA_ROAR = register("monsterverse_godzilla_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> KONG_ROAR = register("kong_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUTO_CALL = register("muto_call");
    public static final DeferredHolder<SoundEvent, SoundEvent> SKULLCRAWLER_HISS = register("skullcrawler_hiss");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHIMO_ROAR = register("shimo_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> SKAR_KING_ROAR = register("skar_king_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> WARBAT_SCREECH = register("warbat_screech");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIAMAT_HISS = register("tiamat_hiss");
    public static final DeferredHolder<SoundEvent, SoundEvent> BEHEMOTH_ROAR = register("behemoth_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> METHUSELAH_RUMBLE = register("methuselah_rumble");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCYLLA_SCREECH = register("scylla_screech");
    public static final DeferredHolder<SoundEvent, SoundEvent> PROTON_SCREAM = register("proton_scream");
    public static final DeferredHolder<SoundEvent, SoundEvent> ORCA_SIGNAL = register("orca_signal");
    public static final DeferredHolder<SoundEvent, SoundEvent> VORTEX_WHOOSH = register("vortex_whoosh");

    // Phase 4
    public static final DeferredHolder<SoundEvent, SoundEvent> GIGAN_SCREECH = register("gigan_screech");
    public static final DeferredHolder<SoundEvent, SoundEvent> HEDORAH_CRY = register("hedorah_cry");
    public static final DeferredHolder<SoundEvent, SoundEvent> MEGALON_SCREECH = register("megalon_screech");
    public static final DeferredHolder<SoundEvent, SoundEvent> JET_JAGUAR_HUM = register("jet_jaguar_hum");
    public static final DeferredHolder<SoundEvent, SoundEvent> EBIRAH_CLICK = register("ebirah_click");
    public static final DeferredHolder<SoundEvent, SoundEvent> KUMONGA_HISS = register("kumonga_hiss");
    public static final DeferredHolder<SoundEvent, SoundEvent> KAMACURAS_SCREECH = register("kamacuras_screech");
    public static final DeferredHolder<SoundEvent, SoundEvent> MINILLA_CRY = register("minilla_cry");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOROSAURUS_ROAR = register("gorosaurus_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> MANDA_ROAR = register("manda_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> BARAGON_ROAR = register("baragon_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> VARAN_CRY = register("varan_cry");
    public static final DeferredHolder<SoundEvent, SoundEvent> KING_CAESAR_ROAR = register("king_caesar_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> TITANOSAURUS_ROAR = register("titanosaurus_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> ORGA_ROAR = register("orga_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> UFO_HUM = register("ufo_hum");
    public static final DeferredHolder<SoundEvent, SoundEvent> MEGAGUIRUS_BUZZ = register("megaguirus_buzz");
    public static final DeferredHolder<SoundEvent, SoundEvent> MEGANULON_CHITTER = register("meganulon_chitter");
    public static final DeferredHolder<SoundEvent, SoundEvent> MILLENNIUM_GODZILLA_ROAR = register("millennium_godzilla_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> GMK_GHIDORAH_ROAR = register("gmk_ghidorah_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> KIRYU_ROAR = register("kiryu_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> MONSTER_X_ROAR = register("monster_x_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> KEIZER_ROAR = register("keizer_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZILLA_ROAR = register("zilla_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOTENGO_HORN = register("gotengo_horn");
    public static final DeferredHolder<SoundEvent, SoundEvent> XILIEN_SAUCER = register("xilien_saucer");
    public static final DeferredHolder<SoundEvent, SoundEvent> BLACK_HOLE = register("black_hole");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALIEN_SIGNAL = register("alien_signal");

    // Phase 5
    public static final DeferredHolder<SoundEvent, SoundEvent> SHIN_ROAR = register("shin_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> MINUS_ONE_ROAR = register("minus_one_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> HEAT_RAY_FIRE = register("heat_ray_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> MINUS_ZERO_GHIDORAH_ROAR = register("minus_zero_ghidorah_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> GODZILLA_EARTH_ROAR = register("godzilla_earth_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> SERVUM_SHRIEK = register("servum_shriek");
    public static final DeferredHolder<SoundEvent, SoundEvent> NANOMETAL_HUM = register("nanometal_hum");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLANET_EATER_HUM = register("planet_eater_hum");
    public static final DeferredHolder<SoundEvent, SoundEvent> SP_GODZILLA_ROAR = register("sp_godzilla_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> RODAN_SP_CRY = register("rodan_sp_cry");
    public static final DeferredHolder<SoundEvent, SoundEvent> SALUNGA_ROAR = register("salunga_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> TITAN_X_CALL = register("titan_x_call");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCARAB_SCREECH = register("scarab_screech");
    public static final DeferredHolder<SoundEvent, SoundEvent> FROST_VARK_ROAR = register("frost_vark_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> ION_DRAGON_SCREECH = register("ion_dragon_screech");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_GROWL = register("kraken_growl");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHINDEN_ENGINE = register("shinden_engine");

    private ModSounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(ReikoGodzilla.id(name)));
    }
}

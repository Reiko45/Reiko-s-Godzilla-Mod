package com.reiko45.godzilla.registry;

import com.reiko45.godzilla.ReikoGodzilla;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/** Damage types live in data/reikogodzilla/damage_type/*.json. */
public final class ModDamageTypes {
    public static final ResourceKey<DamageType> ATOMIC_BREATH = key("atomic_breath");
    public static final ResourceKey<DamageType> GRAVITY_BEAM = key("gravity_beam");
    public static final ResourceKey<DamageType> SPACE_BEAM = key("space_beam");
    public static final ResourceKey<DamageType> MASER = key("maser");
    public static final ResourceKey<DamageType> KAIJU_STOMP = key("kaiju_stomp");
    public static final ResourceKey<DamageType> OXYGEN_DESTROYER = key("oxygen_destroyer");
    public static final ResourceKey<DamageType> NUCLEAR_PULSE = key("nuclear_pulse");
    public static final ResourceKey<DamageType> SONIC_BOOM = key("sonic_boom");
    // Phase 2
    public static final ResourceKey<DamageType> INFINITE_HEAT_RAY = key("infinite_heat_ray");
    public static final ResourceKey<DamageType> RED_SPIRAL_RAY = key("red_spiral_ray");
    public static final ResourceKey<DamageType> URANIUM_HEAT_BEAM = key("uranium_heat_beam");
    public static final ResourceKey<DamageType> CORONA_BEAM = key("corona_beam");
    public static final ResourceKey<DamageType> MICRO_OXYGEN = key("micro_oxygen");
    public static final ResourceKey<DamageType> PRISM_BEAM = key("prism_beam");
    public static final ResourceKey<DamageType> MEGA_BUSTER = key("mega_buster");
    public static final ResourceKey<DamageType> PLASMA_LASER = key("plasma_laser");
    public static final ResourceKey<DamageType> FREEZE_RAY = key("freeze_ray");
    public static final ResourceKey<DamageType> SHOCK_ANCHOR = key("shock_anchor");
    public static final ResourceKey<DamageType> RADIATION = key("radiation");
    public static final ResourceKey<DamageType> ACID_SAP = key("acid_sap");
    public static final ResourceKey<DamageType> MELTDOWN = key("meltdown");

    // Phase 3
    public static final ResourceKey<DamageType> PROTON_SCREAM = key("proton_scream");
    public static final ResourceKey<DamageType> WHIPSLASH = key("whipslash");
    public static final ResourceKey<DamageType> GOD_RAY = key("god_ray");
    public static final ResourceKey<DamageType> ELECTRIC_COIL = key("electric_coil");
    public static final ResourceKey<DamageType> FROST_BREATH = key("frost_breath");
    public static final ResourceKey<DamageType> GRAVITY_INVERSION = key("gravity_inversion");
    // Phase 4
    public static final ResourceKey<DamageType> BUZZSAW = key("buzzsaw");
    public static final ResourceKey<DamageType> HEDRIUM_RAY = key("hedrium_ray");
    public static final ResourceKey<DamageType> ACID_MIST = key("acid_mist");
    public static final ResourceKey<DamageType> BEAST_KILLER = key("beast_killer");
    public static final ResourceKey<DamageType> MAGMA_RAY = key("magma_ray");
    public static final ResourceKey<DamageType> PLASMA_CANNON = key("plasma_cannon");
    public static final ResourceKey<DamageType> ABSOLUTE_ZERO = key("absolute_zero");
    public static final ResourceKey<DamageType> DESTROYED_THUNDER = key("destroyed_thunder");
    public static final ResourceKey<DamageType> GIGARIUM_CLUSTER = key("gigarium_cluster");
    public static final ResourceKey<DamageType> BLACK_HOLE = key("black_hole");
    public static final ResourceKey<DamageType> THUNDER_SPARK = key("thunder_spark");
    public static final ResourceKey<DamageType> ENERGY_DRAIN = key("energy_drain");
    public static final ResourceKey<DamageType> CONSTRICTION = key("constriction");
    public static final ResourceKey<DamageType> TOWER_LASER = key("tower_laser");
    // Phase 5
    public static final ResourceKey<DamageType> SHIN_BREATH = key("shin_breath");
    public static final ResourceKey<DamageType> DORSAL_BEAM = key("dorsal_beam");
    public static final ResourceKey<DamageType> HEAT_RAY = key("heat_ray");
    public static final ResourceKey<DamageType> PRESSURE_WAVE = key("pressure_wave");
    public static final ResourceKey<DamageType> FALLOUT = key("fallout");
    public static final ResourceKey<DamageType> DECOMPRESSION = key("decompression");
    public static final ResourceKey<DamageType> GRAVITY_LIFT = key("gravity_lift");
    public static final ResourceKey<DamageType> CHARGED_PARTICLE = key("charged_particle");
    public static final ResourceKey<DamageType> PLASMA_CUTTER = key("plasma_cutter");
    public static final ResourceKey<DamageType> EM_SHIELD = key("em_shield");
    public static final ResourceKey<DamageType> NANOMETAL = key("nanometal");
    public static final ResourceKey<DamageType> ARCHETYPE = key("archetype");
    public static final ResourceKey<DamageType> ULTIMA_BREATH = key("ultima_breath");
    public static final ResourceKey<DamageType> FROST_INHALE = key("frost_inhale");
    public static final ResourceKey<DamageType> ION_TRAIL = key("ion_trail");
    public static final ResourceKey<DamageType> OVERLOAD = key("overload");
    public static final ResourceKey<DamageType> LIFE_DRAIN = key("life_drain");
    public static final ResourceKey<DamageType> BIOLUMINESCENCE = key("bioluminescence");
    public static final ResourceKey<DamageType> SHOCK_PUNCH = key("shock_punch");
    /** Every beam-type attack: these can be reflected (Super X2's Fire Mirror, shields) or absorbed. */
    public static final net.minecraft.tags.TagKey<DamageType> KAIJU_BEAM =
            net.minecraft.tags.TagKey.create(Registries.DAMAGE_TYPE, ReikoGodzilla.id("kaiju_beam"));

    private ModDamageTypes() {}

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ReikoGodzilla.id(name));
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> type, @Nullable Entity attacker) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type), attacker);
    }
}

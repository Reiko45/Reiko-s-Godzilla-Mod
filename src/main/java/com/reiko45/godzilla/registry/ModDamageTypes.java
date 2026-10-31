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

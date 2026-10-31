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

    private ModDamageTypes() {}

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ReikoGodzilla.id(name));
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> type, @Nullable Entity attacker) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type), attacker);
    }
}

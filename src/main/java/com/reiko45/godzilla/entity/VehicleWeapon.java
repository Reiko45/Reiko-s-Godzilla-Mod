package com.reiko45.godzilla.entity;

import net.minecraft.server.level.ServerPlayer;

/** Rideable things whose weapon the rider fires with the "Fire Weapon" key (default R). */
public interface VehicleWeapon {
    void fireWeapon(ServerPlayer rider);
}

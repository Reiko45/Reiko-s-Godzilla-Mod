package com.reiko45.godzilla.entity;

import net.minecraft.server.level.ServerPlayer;

/**
 * Rideable things the rider can trigger with keys: action 0 = Fire (R), 1 = Special (V), 2 = Transform/Dock (G).
 */
public interface VehicleWeapon {
    void vehicleAction(ServerPlayer rider, int action);
}

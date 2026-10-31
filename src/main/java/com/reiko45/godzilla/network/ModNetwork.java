package com.reiko45.godzilla.network;

import com.reiko45.godzilla.entity.VehicleWeapon;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
    private ModNetwork() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("2");
        registrar.playToServer(FireWeaponPayload.TYPE, FireWeaponPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player && player.getVehicle() instanceof VehicleWeapon weapon) {
                        weapon.vehicleAction(player, payload.action());
                    }
                }));
    }
}

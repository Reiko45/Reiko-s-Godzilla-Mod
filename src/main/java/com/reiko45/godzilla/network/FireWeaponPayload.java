package com.reiko45.godzilla.network;

import com.reiko45.godzilla.ReikoGodzilla;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Sent when the rider presses Fire (0), Special (1) or Transform (2). */
public record FireWeaponPayload(int action) implements CustomPacketPayload {
    public static final Type<FireWeaponPayload> TYPE = new Type<>(ReikoGodzilla.id("fire_weapon"));
    public static final StreamCodec<ByteBuf, FireWeaponPayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(FireWeaponPayload::new, FireWeaponPayload::action);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

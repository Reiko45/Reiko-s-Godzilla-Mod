package com.reiko45.godzilla.network;

import com.reiko45.godzilla.ReikoGodzilla;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Sent when the rider presses the Fire Weapon key. */
public record FireWeaponPayload() implements CustomPacketPayload {
    public static final Type<FireWeaponPayload> TYPE = new Type<>(ReikoGodzilla.id("fire_weapon"));
    public static final StreamCodec<ByteBuf, FireWeaponPayload> STREAM_CODEC = StreamCodec.unit(new FireWeaponPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

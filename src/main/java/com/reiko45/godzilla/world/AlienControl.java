package com.reiko45.godzilla.world;

import com.reiko45.godzilla.entity.KaijuEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Sends an alien control signal. Transmitter blocks and Xilien saucers call {@link #broadcast} every second; a kaiju
 * stays controlled for as long as the signal keeps reaching it.
 */
public final class AlienControl {
    /** How long one broadcast keeps a monster under control. */
    public static final int SIGNAL_TICKS = 60;

    private AlienControl() {}

    /** Returns how many kaiju are under this signal's control. */
    public static int broadcast(ServerLevel level, Vec3 center, double radius, AlienSignal signal) {
        int count = 0;
        for (KaijuEntity k : level.getEntitiesOfClass(KaijuEntity.class, new AABB(center, center).inflate(radius),
                k -> k.distanceToSqr(center) <= radius * radius)) {
            k.receiveSignal(signal, SIGNAL_TICKS);
            if (k.getControllingSignal() == signal) count++;
        }
        return count;
    }
}

package com.reiko45.godzilla.world;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Where the living Mechagodzilla City cores are. Nanometal only grows (and stays active) near one. Cores check in
 * every few seconds; one that stops checking in (destroyed or unloaded) stops counting after a while.
 */
public final class NanometalCores {
    private record Core(ResourceKey<Level> dimension, BlockPos pos, long lastSeen) {}

    private static final Map<UUID, Core> CORES = new HashMap<>();
    private static final long TIMEOUT = 400;

    private NanometalCores() {}

    public static synchronized void checkIn(ServerLevel level, UUID id, BlockPos pos) {
        CORES.put(id, new Core(level.dimension(), pos.immutable(), level.getGameTime()));
    }

    public static synchronized void remove(UUID id) {
        CORES.remove(id);
    }

    public static synchronized boolean isNear(ServerLevel level, BlockPos pos, int radius) {
        long now = level.getGameTime();
        Iterator<Core> it = CORES.values().iterator();
        while (it.hasNext()) {
            Core c = it.next();
            if (now - c.lastSeen > TIMEOUT || now < c.lastSeen) {
                it.remove();
                continue;
            }
            if (c.dimension == level.dimension() && c.pos.distSqr(pos) <= (double) radius * radius) return true;
        }
        return false;
    }
}

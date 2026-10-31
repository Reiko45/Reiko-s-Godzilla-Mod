package com.reiko45.godzilla.entity;

import net.minecraft.world.phys.Vec3;

/** Implemented by entities whose beams are drawn by {@code BeamRenderer}. All values must be readable client-side. */
public interface BeamEmitter {
    /** Number of beams currently firing (0 when idle). */
    int getActiveBeamCount();

    Vec3 getBeamOrigin(int index, float partialTick);

    Vec3 getBeamEnd(int index);

    BeamStyle getBeamStyle();

    /** Beam radius in blocks. */
    float getBeamWidth();
}

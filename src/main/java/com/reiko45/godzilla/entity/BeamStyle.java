package com.reiko45.godzilla.entity;

/** How a beam looks on the client. Colours are 0-1 RGB for the glow and the core. */
public enum BeamStyle {
    /** Godzilla's classic blue atomic breath. */
    ATOMIC(0.25f, 0.55f, 1.0f, 0.85f, 0.95f, 1.0f, false, false),
    /** King Ghidorah's golden, crackling gravity beams. */
    GRAVITY(1.0f, 0.6f, 0.05f, 1.0f, 0.85f, 0.3f, true, false),
    /** Mechagodzilla's eye beam, cycling through the spectrum. */
    SPACE_BEAM(1.0f, 0.3f, 0.3f, 1.0f, 1.0f, 1.0f, false, true),
    /** Human maser cannons: thin, pale cyan, slightly unstable. */
    MASER(0.4f, 0.95f, 1.0f, 0.9f, 1.0f, 1.0f, true, false);

    public final float r, g, b, coreR, coreG, coreB;
    public final boolean jagged;
    public final boolean rainbow;

    BeamStyle(float r, float g, float b, float coreR, float coreG, float coreB, boolean jagged, boolean rainbow) {
        this.r = r; this.g = g; this.b = b;
        this.coreR = coreR; this.coreG = coreG; this.coreB = coreB;
        this.jagged = jagged;
        this.rainbow = rainbow;
    }

    public static BeamStyle byId(int id) {
        BeamStyle[] v = values();
        return id >= 0 && id < v.length ? v[id] : ATOMIC;
    }
}

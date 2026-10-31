package com.reiko45.godzilla.entity;

import net.minecraft.world.phys.Vec3;

/** How a beam looks on the client. Colours are 0-1 RGB for the outer glow and the bright core. */
public enum BeamStyle {
    /** Godzilla's classic blue atomic breath. */
    ATOMIC(0.25f, 0.55f, 1.0f, 0.85f, 0.95f, 1.0f),
    /** King Ghidorah's golden, crackling gravity beams. */
    GRAVITY(1.0f, 0.6f, 0.05f, 1.0f, 0.85f, 0.3f, Shape.JAGGED),
    /** Showa Mechagodzilla's eye beam, cycling through the spectrum. */
    SPACE_BEAM(1.0f, 0.3f, 0.3f, 1.0f, 1.0f, 1.0f, Shape.RAINBOW),
    /** Human maser cannons: thin, pale cyan, slightly unstable. */
    MASER(0.4f, 0.95f, 1.0f, 0.9f, 1.0f, 1.0f, Shape.JAGGED),
    // ---- Phase 2 (Heisei)
    /** Heisei Godzilla's Red Spiral Ray after absorbing Fire Rodan's energy. */
    SPIRAL_RED(1.0f, 0.12f, 0.08f, 1.0f, 0.85f, 0.45f, Shape.SPIRAL),
    /** Burning Godzilla's Infinite Heat Ray. */
    INFINITE_HEAT(1.0f, 0.3f, 0.05f, 1.0f, 0.9f, 0.6f, Shape.SPIRAL),
    /** Fire Rodan's Uranium Heat Beam. */
    URANIUM(1.0f, 0.55f, 0.1f, 1.0f, 0.95f, 0.7f),
    /** SpaceGodzilla's homing Corona Beam: it arcs toward its target. */
    CORONA(0.55f, 0.3f, 1.0f, 0.9f, 0.8f, 1.0f, Shape.CURVED),
    /** Destoroyah's Micro-Oxygen beam. */
    MICRO_OXYGEN(0.9f, 0.15f, 0.55f, 1.0f, 0.7f, 0.9f),
    /** Battra's prism beams. */
    PRISM(0.7f, 0.2f, 1.0f, 1.0f, 0.8f, 1.0f),
    /** Electric bolts (Battra's horn). */
    ELECTRIC(0.95f, 0.9f, 0.35f, 1.0f, 1.0f, 0.9f, Shape.JAGGED),
    /** Heisei Mechagodzilla's Mega-Buster and Plasma Grenade. */
    MEGA_BUSTER(1.0f, 0.7f, 0.15f, 1.0f, 1.0f, 0.8f),
    /** MOGUERA's plasma laser cannons. */
    PLASMA(0.3f, 0.6f, 1.0f, 0.9f, 0.95f, 1.0f),
    /** Freezer weapons (Super X3 cryo laser, DAG-MB96). */
    FREEZE(0.7f, 0.95f, 1.0f, 1.0f, 1.0f, 1.0f, Shape.RINGS),
    /** Shock Anchor cables. */
    ANCHOR(0.95f, 0.85f, 0.35f, 0.8f, 0.8f, 0.85f, Shape.JAGGED),
    /** Little Godzilla's harmless-looking bubble breath. */
    BUBBLE(1.0f, 0.45f, 0.4f, 1.0f, 0.8f, 0.7f, Shape.RINGS);

    public enum Shape { STRAIGHT, JAGGED, RAINBOW, SPIRAL, CURVED, RINGS }

    public final float r, g, b, coreR, coreG, coreB;
    public final Shape shape;
    public final boolean jagged;
    public final boolean rainbow;

    BeamStyle(float r, float g, float b, float coreR, float coreG, float coreB) {
        this(r, g, b, coreR, coreG, coreB, Shape.STRAIGHT);
    }

    BeamStyle(float r, float g, float b, float coreR, float coreG, float coreB, Shape shape) {
        this.r = r; this.g = g; this.b = b;
        this.coreR = coreR; this.coreG = coreG; this.coreB = coreB;
        this.shape = shape;
        this.jagged = shape == Shape.JAGGED;
        this.rainbow = shape == Shape.RAINBOW;
    }

    /**
     * Point at {@code t} (0..1) along the beam. Curved beams follow a quadratic arc that bulges upward; everything
     * else is straight. Shared by the server (hit detection) and the client (drawing) so both agree.
     */
    public Vec3 pointAt(Vec3 from, Vec3 to, double t) {
        if (shape != Shape.CURVED) return from.lerp(to, t);
        double len = from.distanceTo(to);
        Vec3 control = from.lerp(to, 0.5).add(0, len * 0.3, 0);
        double u = 1 - t;
        return from.scale(u * u).add(control.scale(2 * u * t)).add(to.scale(t * t));
    }

    public static BeamStyle byId(int id) {
        BeamStyle[] v = values();
        return id >= 0 && id < v.length ? v[id] : ATOMIC;
    }
}

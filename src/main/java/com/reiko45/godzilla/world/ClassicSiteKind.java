package com.reiko45.godzilla.world;

import net.minecraft.util.StringRepresentable;

/** Showa and Millennium locations built by {@link ClassicSitePiece}. */
public enum ClassicSiteKind implements StringRepresentable {
    /** World Children's Land and the Godzilla Tower: the Nebula M control room and its mouth laser (Godzilla vs. Gigan). */
    GODZILLA_TOWER("godzilla_tower", 16, 34, Placement.SURFACE),
    /** Seatopia, the underground kingdom of Megalon's worshippers (Godzilla vs. Megalon). */
    SEATOPIA("seatopia", 24, 18, Placement.DEEP),
    /** King Caesar's cliff and the Azumi shrine in Okinawa (Godzilla vs. Mechagodzilla). */
    AZUMI_SHRINE("azumi_shrine", 18, 20, Placement.SURFACE),
    /** GMK: Baragon's shrine in a mountain tunnel (Mount Myoko). */
    BARAGON_SHRINE("baragon_shrine", 12, 14, Placement.SURFACE),
    /** GMK: Mothra's lake shrine (Lake Ikeda). */
    MOTHRA_SHRINE("mothra_shrine", 12, 10, Placement.SURFACE),
    /** GMK: King Ghidorah's ice cave under the forest by Mount Fuji. */
    GHIDORAH_SHRINE("ghidorah_shrine", 11, 12, Placement.SURFACE),
    /** The Millennian UFO lying on the sea floor (Godzilla 2000: Millennium). */
    UFO_WRECK("ufo_wreck", 10, 10, Placement.SEA_FLOOR),
    /** Flooded ruins full of Meganulon egg masses (Godzilla vs. Megaguirus). */
    MEGANULON_NEST("meganulon_nest", 14, 10, Placement.SURFACE),
    /** Kiryu's maintenance hangar with its charging gantries (Godzilla Against Mechagodzilla). */
    KIRYU_HANGAR("kiryu_hangar", 14, 18, Placement.SURFACE),
    /** A landed Xilien mothership: the Xilien control core, Monster X and their saucers (Godzilla Final Wars). */
    XILIEN_MOTHERSHIP("xilien_mothership", 22, 18, Placement.SURFACE);

    public enum Placement { SURFACE, SEA_FLOOR, DEEP }

    public final int radius;
    public final int headroom;
    public final Placement placement;
    private final String name;

    ClassicSiteKind(String name, int radius, int headroom, Placement placement) {
        this.name = name;
        this.radius = radius;
        this.headroom = headroom;
        this.placement = placement;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static ClassicSiteKind byName(String name) {
        for (ClassicSiteKind k : values()) if (k.name.equals(name)) return k;
        return GODZILLA_TOWER;
    }
}

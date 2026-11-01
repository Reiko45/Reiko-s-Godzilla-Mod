package com.reiko45.godzilla.world;

import net.minecraft.util.StringRepresentable;

/** Showa, Millennium and modern locations built by {@link ClassicSitePiece}. */
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
    XILIEN_MOTHERSHIP("xilien_mothership", 22, 18, Placement.SURFACE),
    // Phase 5: Reiwa and modern
    /** Ruined Tokyo blocks rigged for Operation Yashiori: towers with demolition charges, an elevated line, supply dumps (Shin Godzilla). */
    TOKYO_RUINS("tokyo_ruins", 26, 30, Placement.SURFACE),
    /** The Odo Island garrison: airstrip, hangar with a damaged Shinden, and the island's legendary creature (Godzilla Minus One). */
    ODO_ISLAND("odo_island", 20, 12, Placement.SURFACE),
    /** The Operation Wada Tsumi dock: destroyers, Freon depots and balloon rigs (Godzilla Minus One). */
    WADATSUMI_DOCK("wadatsumi_dock", 18, 12, Placement.SURFACE),
    /** Matsumoto's castle town in 1949, where Ghidorah first strikes (Godzilla Minus Zero). */
    MATSUMOTO("matsumoto", 22, 20, Placement.SURFACE),
    /** Mechagodzilla City: a nanometal crater of spike towers around the head core (City on the Edge of Battle). */
    MECHAGODZILLA_CITY("mechagodzilla_city", 26, 32, Placement.SURFACE),
    /** The Houtua's village in a metallic forest (the anime trilogy). */
    HOUTUA_VILLAGE("houtua_village", 20, 14, Placement.SURFACE),
    /** A ruined Exif temple holding the Garbetrium totem (The Planet Eater). */
    EXIF_OBSERVATORY("exif_observatory", 14, 18, Placement.SURFACE),
    /** The Otaki Factory and the Misakioku antenna, where Jet Jaguar was built (Godzilla Singular Point). */
    OTAKI_FACTORY("otaki_factory", 16, 24, Placement.SURFACE),
    /** SHIVA, the underground facility over a Red Dust pool where Salunga is held (Godzilla Singular Point). */
    SHIVA_FACILITY("shiva_facility", 18, 12, Placement.DEEP),
    /** An Apex Cybernetics lab with Titan X's stolen egg (Monarch: Legacy of Monsters). */
    APEX_LAB("apex_lab", 18, 14, Placement.SURFACE),
    /** The coastal shrine to "El Gran Dios del Mar" at Santa Soledad (Monarch: Legacy of Monsters). */
    SANTA_SOLEDAD("santa_soledad", 14, 12, Placement.SURFACE),
    /** Monarch's Outpost 88 in the Alaskan snow, over a sealed rift (Monarch: Legacy of Monsters). */
    OUTPOST_88("outpost_88", 14, 12, Placement.SURFACE);

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

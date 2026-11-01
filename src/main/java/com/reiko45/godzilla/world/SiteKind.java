package com.reiko45.godzilla.world;

import net.minecraft.util.StringRepresentable;

/** MonsterVerse locations built by {@link MonsterverseSitePiece}. */
public enum SiteKind implements StringRepresentable {
    /** Kong's ancestral throne room in the Hollow Earth, with the Battle Axe. */
    THRONE_ROOM("throne_room", 18, 26, true),
    /** The Skar King's lava cavern, where Shimo is chained. */
    SKAR_KINGDOM("skar_kingdom", 24, 28, true),
    /** Tiamat's ice-domed lair under an Arctic sea, full of stored solar radiation. */
    TIAMAT_LAIR("tiamat_lair", 14, 14, false),
    /** A vortex into the Hollow Earth in the snowy wastes (the Antarctic vortex). */
    VORTEX("vortex", 10, 10, false),
    /** A vortex back to the surface, inside the Hollow Earth. */
    HOLLOW_VORTEX("hollow_vortex", 4, 6, true),
    /** Methuselah sleeping in the mountains. */
    METHUSELAH("methuselah", 2, 2, false),
    /** Behemoth roaming the rainforest. */
    BEHEMOTH("behemoth", 2, 2, false);

    public final int radius;
    public final int headroom;
    public final boolean underground;
    private final String name;

    SiteKind(String name, int radius, int headroom, boolean underground) {
        this.name = name;
        this.radius = radius;
        this.headroom = headroom;
        this.underground = underground;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static SiteKind byName(String name) {
        for (SiteKind k : values()) if (k.name.equals(name)) return k;
        return VORTEX;
    }
}

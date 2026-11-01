package com.reiko45.godzilla.world;

import net.minecraft.util.StringRepresentable;

public enum IslandKind implements StringRepresentable {
    MONSTER("monster", 52),
    INFANT("infant", 40),
    /** Kong: Skull Island - jungle, a skull-faced mountain, the Iwi wall, Skullcrawler caves and a Hollow Earth vortex. */
    SKULL("skull", 58);

    public final int radius;
    private final String name;

    IslandKind(String name, int radius) {
        this.name = name;
        this.radius = radius;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static IslandKind byName(String name) {
        for (IslandKind k : values()) if (k.name.equals(name)) return k;
        return MONSTER;
    }
}

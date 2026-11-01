package com.reiko45.godzilla.world;

import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;
import org.joml.Vector3f;

/**
 * Alien mind-control signals from the Showa and Millennium films. Each one only works on certain monsters
 * (see {@link com.reiko45.godzilla.entity.KaijuEntity#controllableBy}), and controlled monsters fight for the aliens
 * until the signal stops: destroy the transmitter (or the saucers carrying it) and they're freed.
 */
public enum AlienSignal implements StringRepresentable {
    /** The Space Hunter Nebula M's orders to Gigan and King Ghidorah, sent from the Godzilla Tower (Godzilla vs. Gigan). */
    NEBULA_M("nebula_m", ChatFormatting.LIGHT_PURPLE, 0xC050FF),
    /** Seatopia's guidance for Megalon, and the hijacking of Jet Jaguar (Godzilla vs. Megalon). */
    SEATOPIA("seatopia", ChatFormatting.AQUA, 0x40E0D0),
    /** The Kilaaks' control of every monster on Monster Island (Destroy All Monsters). */
    KILAAK("kilaak", ChatFormatting.GOLD, 0xFFB020),
    /** The Black Hole Planet 3 aliens' control devices: Titanosaurus's implant, Mechagodzilla 2's link (Terror of Mechagodzilla). */
    BLACK_HOLE("black_hole", ChatFormatting.DARK_GRAY, 0x606070),
    /** The Xiliens' control of monsters carrying M-Base (Final Wars). Godzilla doesn't have it. */
    XILIEN("xilien", ChatFormatting.RED, 0xFF3030);

    private final String name;
    public final ChatFormatting chat;
    public final int rgb;

    AlienSignal(String name, ChatFormatting chat, int rgb) {
        this.name = name;
        this.chat = chat;
        this.rgb = rgb;
    }

    public Vector3f color() {
        return new Vector3f(((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f);
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static AlienSignal byId(int id) {
        AlienSignal[] v = values();
        return v[Math.floorMod(id, v.length)];
    }

    public AlienSignal next() {
        return byId(ordinal() + 1);
    }
}

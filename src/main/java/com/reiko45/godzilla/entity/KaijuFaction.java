package com.reiko45.godzilla.entity;

import net.minecraft.world.BossEvent;

/**
 * Who fights whom. Titans (Godzilla, Rodan, Anguirus) and Guardians (Mothra) leave players alone unless provoked
 * and battle Invaders (King Ghidorah, Mechagodzilla). Human tech (Maser Tanks) sides with Titans and Guardians.
 */
public enum KaijuFaction {
    TITAN(BossEvent.BossBarColor.BLUE),
    GUARDIAN(BossEvent.BossBarColor.YELLOW),
    INVADER(BossEvent.BossBarColor.RED),
    HUMAN(BossEvent.BossBarColor.GREEN);

    public final BossEvent.BossBarColor barColor;

    KaijuFaction(BossEvent.BossBarColor barColor) {
        this.barColor = barColor;
    }

    public boolean isHostileTo(KaijuFaction other) {
        return this == INVADER ? other != INVADER : other == INVADER;
    }
}

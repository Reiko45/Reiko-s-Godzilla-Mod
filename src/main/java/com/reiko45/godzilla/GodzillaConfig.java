package com.reiko45.godzilla;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class GodzillaConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.DoubleValue KAIJU_SCALE;
    public static final ModConfigSpec.BooleanValue KAIJU_GRIEFING;
    public static final ModConfigSpec.BooleanValue BEAM_BLOCK_DAMAGE;
    public static final ModConfigSpec.BooleanValue BEAM_FIRE;
    public static final ModConfigSpec.IntValue MAX_BLOCKS_PER_TICK;
    public static final ModConfigSpec.DoubleValue MAX_BLOCK_RESISTANCE;
    public static final ModConfigSpec.DoubleValue PLAYER_DAMAGE_MULTIPLIER;
    public static final ModConfigSpec.BooleanValue BOSS_BARS;
    public static final ModConfigSpec.BooleanValue GODZILLA_HOSTILE;

    public static final ModConfigSpec.BooleanValue EVENTS_ENABLED;
    public static final ModConfigSpec.IntValue EVENT_FIRST_DAY;
    public static final ModConfigSpec.IntValue EVENT_INTERVAL_TICKS;
    public static final ModConfigSpec.DoubleValue EVENT_CHANCE;
    public static final ModConfigSpec.IntValue MAX_KAIJU_NEAR_PLAYER;

    public static final ModConfigSpec.IntValue MOTHRA_GROWTH_TICKS;
    public static final ModConfigSpec.BooleanValue MASER_AUTO_FIRE;
    public static final ModConfigSpec.DoubleValue MASER_DAMAGE;
    public static final ModConfigSpec.DoubleValue OXYGEN_DESTROYER_KAIJU_DAMAGE;

    // Phase 2
    public static final ModConfigSpec.BooleanValue BURNING_GODZILLA;
    public static final ModConfigSpec.IntValue MELTDOWN_TICKS;
    public static final ModConfigSpec.BooleanValue MELTDOWN_EXPLOSION;
    public static final ModConfigSpec.IntValue FROZEN_TICKS;
    public static final ModConfigSpec.IntValue JUNIOR_GROWTH_TICKS;
    public static final ModConfigSpec.DoubleValue DESTOROYAH_CHANCE;
    public static final ModConfigSpec.IntValue BIOLLANTE_GROWTH_STAGES;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.push("kaiju");
        KAIJU_SCALE = b.comment("Size multiplier for every kaiju. 1.0 = one fifth of film size (Showa Godzilla ~10 blocks, Heisei ~20).")
                .defineInRange("scale", 1.0, 0.25, 3.0);
        GODZILLA_HOSTILE = b.comment("If true, Godzilla attacks players on sight (1954 behaviour). Otherwise he only fights back.")
                .define("godzillaHostileToPlayers", false);
        BOSS_BARS = b.comment("Show boss bars for kaiju within 96 blocks.").define("bossBars", true);
        PLAYER_DAMAGE_MULTIPLIER = b.comment("Multiplier for kaiju beam and ability damage dealt to players.")
                .defineInRange("playerDamageMultiplier", 1.0, 0.0, 10.0);
        MOTHRA_GROWTH_TICKS = b.comment("Ticks a Mothra larva must live (out of combat) before it spins a cocoon. 24000 = one day.")
                .defineInRange("mothraGrowthTicks", 12000, 200, 1_000_000);
        b.pop();

        b.push("destruction");
        KAIJU_GRIEFING = b.comment("Kaiju can break blocks (also respects the mobGriefing game rule).").define("kaijuGriefing", true);
        BEAM_BLOCK_DAMAGE = b.comment("Beams such as atomic breath destroy blocks where they hit.").define("beamBlockDamage", true);
        BEAM_FIRE = b.comment("Atomic breath and similar beams start fires.").define("beamFire", true);
        MAX_BLOCKS_PER_TICK = b.comment("Most blocks a single kaiju may break per tick (performance limit).")
                .defineInRange("maxBlocksPerTick", 48, 0, 1024);
        MAX_BLOCK_RESISTANCE = b.comment("Blocks with explosion resistance at or above this are never broken (obsidian is 1200).")
                .defineInRange("maxBlockResistance", 1200.0, 0.0, 3_600_000.0);
        b.pop();

        b.push("events");
        EVENTS_ENABLED = b.comment("Random kaiju events: Godzilla surfacing offshore, Ghidorah meteors, Mechagodzilla invasions.")
                .define("enabled", true);
        EVENT_FIRST_DAY = b.comment("No events before this in-game day.").defineInRange("firstDay", 5, 0, 10_000);
        EVENT_INTERVAL_TICKS = b.comment("Average ticks between event rolls (72000 = 3 in-game days).")
                .defineInRange("intervalTicks", 72000, 1200, 10_000_000);
        EVENT_CHANCE = b.comment("Chance that an event roll actually spawns something.").defineInRange("chance", 0.5, 0.0, 1.0);
        MAX_KAIJU_NEAR_PLAYER = b.comment("Skip events if this many kaiju are already within 256 blocks of the chosen player.")
                .defineInRange("maxKaijuNearPlayer", 2, 1, 64);
        b.pop();

        b.push("weapons");
        MASER_AUTO_FIRE = b.comment("Unmanned Maser Tanks automatically fire at hostile kaiju.").define("maserAutoFire", true);
        MASER_DAMAGE = b.comment("Damage per maser pulse (fires every 3 ticks while the beam is on).")
                .defineInRange("maserDamage", 8.0, 0.0, 1000.0);
        OXYGEN_DESTROYER_KAIJU_DAMAGE = b.comment("Fraction of a kaiju's max health removed per Oxygen Destroyer pulse (10 pulses).")
                .defineInRange("oxygenDestroyerKaijuDamage", 0.09, 0.0, 1.0);
        b.pop();

        b.push("heisei");
        BURNING_GODZILLA = b.comment("Heisei Godzilla goes into meltdown (Burning Godzilla) when badly hurt.").define("burningGodzilla", true);
        MELTDOWN_TICKS = b.comment("Ticks from the start of Burning Godzilla until meltdown, if nothing cools him down (6000 = 5 minutes).")
                .defineInRange("meltdownTicks", 6000, 600, 1_000_000);
        MELTDOWN_EXPLOSION = b.comment("Meltdown causes a huge explosion (also needs kaijuGriefing for block damage).").define("meltdownExplosion", true);
        FROZEN_TICKS = b.comment("How long a kaiju stays frozen solid once its freeze meter fills.").defineInRange("frozenTicks", 120, 20, 2400);
        JUNIOR_GROWTH_TICKS = b.comment("Ticks for Baby Godzilla to grow to Little Godzilla, and again from Little to Junior.")
                .defineInRange("juniorGrowthTicks", 24000, 200, 10_000_000);
        DESTOROYAH_CHANCE = b.comment("Chance that using an Oxygen Destroyer awakens Destoroyah juveniles a few minutes later.")
                .defineInRange("destoroyahChance", 0.35, 0.0, 1.0);
        BIOLLANTE_GROWTH_STAGES = b.comment("Random-tick growth stages before a G-Cell Rose becomes Biollante.")
                .defineInRange("biollanteGrowthStages", 6, 1, 100);
        b.pop();

        SPEC = b.build();
    }

    private GodzillaConfig() {}

    public static double get(ModConfigSpec.DoubleValue v, double fallback) {
        try { return v.get(); } catch (IllegalStateException e) { return fallback; }
    }

    public static int get(ModConfigSpec.IntValue v, int fallback) {
        try { return v.get(); } catch (IllegalStateException e) { return fallback; }
    }

    public static boolean get(ModConfigSpec.BooleanValue v, boolean fallback) {
        try { return v.get(); } catch (IllegalStateException e) { return fallback; }
    }
}

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

    // Phase 3
    public static final ModConfigSpec.BooleanValue ALPHA_CALL;
    public static final ModConfigSpec.IntValue KONG_GROWTH_TICKS;
    public static final ModConfigSpec.DoubleValue APEX_TAKEOVER_CHANCE;
    public static final ModConfigSpec.BooleanValue ICE_AGE;
    public static final ModConfigSpec.IntValue ORCA_RANGE;
    public static final ModConfigSpec.DoubleValue VORTEX_DAMAGE;
    // Phase 4
    public static final ModConfigSpec.IntValue CONTROL_RANGE;
    public static final ModConfigSpec.IntValue SAUCER_CONTROL_RANGE;
    public static final ModConfigSpec.DoubleValue HEDORAH_GROWTH;
    public static final ModConfigSpec.IntValue MINILLA_GROWTH_TICKS;
    public static final ModConfigSpec.IntValue JET_JAGUAR_GIANT_TICKS;
    public static final ModConfigSpec.DoubleValue KIRYU_RAMPAGE_CHANCE;
    public static final ModConfigSpec.DoubleValue DIMENSION_TIDE_RADIUS;
    public static final ModConfigSpec.IntValue DIMENSION_TIDE_COOLDOWN;
    public static final ModConfigSpec.IntValue SHIN_DORMANCY_TICKS;
    public static final ModConfigSpec.DoubleValue SHIN_EVOLUTION_RATE;
    public static final ModConfigSpec.DoubleValue MINUS_ZERO_SCALE;
    public static final ModConfigSpec.DoubleValue GODZILLA_EARTH_SCALE;
    public static final ModConfigSpec.IntValue FILIUS_GROWTH_TICKS;
    public static final ModConfigSpec.IntValue NANOMETAL_RADIUS;
    public static final ModConfigSpec.BooleanValue RED_DUST_SPREADS;
    public static final ModConfigSpec.DoubleValue ULTIMA_MAX_GROWTH;
    public static final ModConfigSpec.IntValue MOP_COOLDOWN;
    public static final ModConfigSpec.BooleanValue SUPERNOVA_PLACEHOLDER;

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

        b.push("monsterverse");
        ALPHA_CALL = b.comment("MonsterVerse King Ghidorah's Alpha Call turns Rodan, Behemoth, Methuselah and Scylla against everyone.")
                .define("alphaCall", true);
        KONG_GROWTH_TICKS = b.comment("Ticks for a young Kong (Skull Island, 1973) to grow into an adult. Feeding him fruit speeds it up.")
                .defineInRange("kongGrowthTicks", 48000, 200, 10_000_000);
        APEX_TAKEOVER_CHANCE = b.comment("Chance, every 10 seconds in a fight, that King Ghidorah's mind takes over a fully powered Apex Mechagodzilla.")
                .defineInRange("apexTakeoverChance", 0.08, 0.0, 1.0);
        ICE_AGE = b.comment("A Shimo controlled by the Skar King turns the weather to snow and freezes the land around her.")
                .define("iceAge", true);
        ORCA_RANGE = b.comment("Range in blocks of the ORCA's calm and lure signals (the Alpha signal reaches 2.5x as far).")
                .defineInRange("orcaRange", 96, 8, 512);
        VORTEX_DAMAGE = b.comment("Damage from the gravity inversion when passing through a Hollow Earth vortex without a HEAV.")
                .defineInRange("vortexDamage", 6.0, 0.0, 100.0);
        b.pop();

        b.push("showa_millennium");
        CONTROL_RANGE = b.comment("Range in blocks of an alien control transmitter (Godzilla Tower, Seatopia, the Xilien mothership...).")
                .defineInRange("controlRange", 160, 16, 1024);
        SAUCER_CONTROL_RANGE = b.comment("Range in blocks of the Xilien control relay carried by each Xilien saucer.")
                .defineInRange("saucerControlRange", 96, 8, 512);
        HEDORAH_GROWTH = b.comment("How quickly Hedorah grows from the pollution it feeds on (0 stops it growing).")
                .defineInRange("hedorahGrowth", 1.0, 0.0, 100.0);
        MINILLA_GROWTH_TICKS = b.comment("Ticks for a newly hatched Minilla to grow up.")
                .defineInRange("minillaGrowthTicks", 24000, 200, 10_000_000);
        JET_JAGUAR_GIANT_TICKS = b.comment("How long Jet Jaguar stays giant-sized each time he grows.")
                .defineInRange("jetJaguarGiantTicks", 2400, 100, 1_000_000);
        KIRYU_RAMPAGE_CHANCE = b.comment("Chance that a Godzilla's roar wakes the first Godzilla's spirit inside Kiryu (Kiryu Kai: 40% of this).")
                .defineInRange("kiryuRampageChance", 0.25, 0.0, 1.0);
        DIMENSION_TIDE_RADIUS = b.comment("Radius in blocks of the Dimension Tide's micro black hole.")
                .defineInRange("dimensionTideRadius", 8.0, 1.0, 32.0);
        DIMENSION_TIDE_COOLDOWN = b.comment("Ticks before the Dimension Tide can fire again (the film's satellite needed an hour).")
                .defineInRange("dimensionTideCooldown", 12000, 0, 1_000_000);
        b.pop();

        b.push("reiwa");
        SHIN_DORMANCY_TICKS = b.comment("How long Shin Godzilla stays dormant after running out of energy (the film: days; default 5 minutes).")
                .defineInRange("shinDormancyTicks", 6000, 200, 2_000_000);
        SHIN_EVOLUTION_RATE = b.comment("How quickly Shin Godzilla evolves into its next form (0 stops it evolving).")
                .defineInRange("shinEvolutionRate", 1.0, 0.0, 100.0);
        MINUS_ZERO_SCALE = b.comment("Size of the Minus Zero Godzilla relative to the Minus One Godzilla (no official height yet).")
                .defineInRange("minusZeroScale", 1.2, 0.5, 4.0);
        GODZILLA_EARTH_SCALE = b.comment("Size of the full-grown Godzilla Earth relative to Filius (6.0 is the true 300 m at the mod's 1/5 scale).")
                .defineInRange("godzillaEarthScale", 4.8, 1.0, 6.0);
        FILIUS_GROWTH_TICKS = b.comment("Ticks for Godzilla Filius to grow into Godzilla Earth.")
                .defineInRange("filiusGrowthTicks", 480000, 200, 100_000_000);
        NANOMETAL_RADIUS = b.comment("How far from a Mechagodzilla City core its nanometal stays active and keeps spreading.")
                .defineInRange("nanometalRadius", 48, 4, 256);
        RED_DUST_SPREADS = b.comment("Whether Red Dust creeps outward on its own (it only spreads a few generations from where it fell).")
                .define("redDustSpreads", true);
        ULTIMA_MAX_GROWTH = b.comment("How much Godzilla Ultima can grow surrounded by Red Dust (5.0 for the full Catastrophe).")
                .defineInRange("ultimaMaxGrowth", 2.0, 1.0, 5.0);
        MOP_COOLDOWN = b.comment("Ticks before the MOP II designator can call in another strike.")
                .defineInRange("mopCooldown", 12000, 0, 1_000_000);
        SUPERNOVA_PLACEHOLDER = b.comment("Lets the placeholder MonsterVerse SpaceGodzilla (Godzilla x Kong: Supernova, 2027; design unrevealed) appear in events.")
                .define("supernovaPlaceholder", false);
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

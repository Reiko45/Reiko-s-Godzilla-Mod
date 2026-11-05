package com.reiko45.godzilla.registry;

import com.reiko45.godzilla.ReikoGodzilla;
import com.reiko45.godzilla.item.LoreItem;
import com.reiko45.godzilla.item.VehicleItem;
import com.reiko45.godzilla.item.OxygenDestroyerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ReikoGodzilla.MODID);

    // Weapons & tech
    public static final DeferredItem<OxygenDestroyerItem> OXYGEN_DESTROYER = ITEMS.register("oxygen_destroyer",
            () -> new OxygenDestroyerItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<VehicleItem> MASER_TANK = ITEMS.register("maser_tank",
            () -> new VehicleItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), ModEntities.MASER_TANK, "maser_tank"));
    // Phase 2: G-Force tech
    public static final DeferredItem<VehicleItem> FREEZER_TANK = ITEMS.register("freezer_tank",
            () -> new VehicleItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), ModEntities.FREEZER_TANK, "freezer_tank"));
    public static final DeferredItem<VehicleItem> SUPER_X = ITEMS.register("super_x",
            () -> new VehicleItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), ModEntities.SUPER_X, "super_x"));
    public static final DeferredItem<VehicleItem> SUPER_X2 = ITEMS.register("super_x2",
            () -> new VehicleItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), ModEntities.SUPER_X2, "super_x2"));
    public static final DeferredItem<VehicleItem> SUPER_X3 = ITEMS.register("super_x3",
            () -> new VehicleItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), ModEntities.SUPER_X3, "super_x3"));
    public static final DeferredItem<VehicleItem> GARUDA = ITEMS.register("garuda",
            () -> new VehicleItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), ModEntities.GARUDA, "garuda"));
    public static final DeferredItem<VehicleItem> GFORCE_MECHAGODZILLA = ITEMS.register("gforce_mechagodzilla",
            () -> new VehicleItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), ModEntities.GFORCE_MECHAGODZILLA, "gforce_mechagodzilla"));
    public static final DeferredItem<VehicleItem> MOGUERA = ITEMS.register("moguera",
            () -> new VehicleItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), ModEntities.MOGUERA, "moguera"));

    // Phase 3: MonsterVerse tech
    public static final DeferredItem<VehicleItem> HEAV = ITEMS.register("heav",
            () -> new VehicleItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), ModEntities.HEAV, "heav"));
    public static final DeferredItem<VehicleItem> APEX_MECHAGODZILLA = ITEMS.register("apex_mechagodzilla",
            () -> new VehicleItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), ModEntities.APEX_MECHAGODZILLA, "apex_mechagodzilla"));
    public static final DeferredItem<com.reiko45.godzilla.item.KongAxeItem> KONG_AXE = ITEMS.register("kong_axe",
            () -> new com.reiko45.godzilla.item.KongAxeItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<LoreItem> BEAST_GLOVE = ITEMS.register("beast_glove",
            () -> new LoreItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(), "beast_glove"));
    public static final DeferredItem<LoreItem> GHIDORAH_SKULL = ITEMS.register("ghidorah_skull",
            () -> new LoreItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(), "ghidorah_skull"));
    public static final DeferredItem<LoreItem> HOLLOW_EARTH_CORE = ITEMS.register("hollow_earth_core",
            () -> new LoreItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()
                    .component(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true), "hollow_earth_core"));
    public static final DeferredItem<LoreItem> DORSAL_PLATE = ITEMS.register("dorsal_plate",
            () -> new LoreItem(new Item.Properties().rarity(Rarity.RARE).fireResistant(), "dorsal_plate"));

    // Kaiju drops (materials for later phases' tech)
    public static final DeferredItem<LoreItem> G_CELL = ITEMS.register("g_cell",
            () -> new LoreItem(new Item.Properties().rarity(Rarity.EPIC).fireResistant(), "g_cell"));
    public static final DeferredItem<LoreItem> SPACE_TITANIUM = ITEMS.register("space_titanium",
            () -> new LoreItem(new Item.Properties().rarity(Rarity.RARE).fireResistant(), "space_titanium"));
    public static final DeferredItem<LoreItem> GHIDORAH_SCALE = ITEMS.register("ghidorah_scale",
            () -> new LoreItem(new Item.Properties().rarity(Rarity.RARE), "ghidorah_scale"));
    public static final DeferredItem<LoreItem> MOTHRA_SCALE = ITEMS.register("mothra_scale",
            () -> new LoreItem(new Item.Properties().rarity(Rarity.RARE), "mothra_scale"));
    public static final DeferredItem<LoreItem> KAIJU_SCALE = ITEMS.register("kaiju_scale",
            () -> new LoreItem(new Item.Properties().rarity(Rarity.UNCOMMON), "kaiju_scale"));

    // Blocks
    public static final DeferredItem<BlockItem> MOTHRA_EGG = ITEMS.registerSimpleBlockItem(ModBlocks.MOTHRA_EGG, new Item.Properties().rarity(Rarity.EPIC));
    public static final DeferredItem<BlockItem> GODZILLA_EGG = ITEMS.registerSimpleBlockItem(ModBlocks.GODZILLA_EGG, new Item.Properties().rarity(Rarity.EPIC));
    public static final DeferredItem<BlockItem> G_CELL_ROSE = ITEMS.registerSimpleBlockItem(ModBlocks.G_CELL_ROSE, new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<BlockItem> NUCLEAR_WARHEAD = ITEMS.registerSimpleBlockItem(ModBlocks.NUCLEAR_WARHEAD, new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<BlockItem> SOLAR_CRYSTAL = ITEMS.registerSimpleBlockItem(ModBlocks.SOLAR_CRYSTAL, new Item.Properties().rarity(Rarity.EPIC));
    public static final DeferredItem<BlockItem> MUTO_EGG = ITEMS.registerSimpleBlockItem(ModBlocks.MUTO_EGG, new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<BlockItem> ORCA = ITEMS.registerSimpleBlockItem(ModBlocks.ORCA, new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<BlockItem> HOLLOW_EARTH_VORTEX = ITEMS.registerSimpleBlockItem(ModBlocks.HOLLOW_EARTH_VORTEX, new Item.Properties().rarity(Rarity.EPIC));

    // Spawn eggs
    // Showa keeps the original egg id so eggs already in worlds still work; Heisei gets its own egg.
    public static final DeferredItem<DeferredSpawnEggItem> GODZILLA_SPAWN_EGG = variantEgg("godzilla", ModEntities.GODZILLA, 0x2E302E, 0xE6E1C8, 0);
    public static final DeferredItem<DeferredSpawnEggItem> HEISEI_GODZILLA_SPAWN_EGG = variantEgg("heisei_godzilla", ModEntities.GODZILLA, 0x3A3836, 0x7FD8FF, 1);
    public static final DeferredItem<DeferredSpawnEggItem> KING_GHIDORAH_SPAWN_EGG = ITEMS.register("king_ghidorah_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.KING_GHIDORAH, 0xD4A017, 0xFFF1A8, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MOTHRA_LARVA_SPAWN_EGG = ITEMS.register("mothra_larva_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.MOTHRA_LARVA, 0x6B4A2B, 0xC9A66B, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MOTHRA_SPAWN_EGG = ITEMS.register("mothra_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.MOTHRA, 0xE8A23A, 0x3B2A80, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> RODAN_SPAWN_EGG = ITEMS.register("rodan_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.RODAN, 0x8A3B1E, 0xE0B070, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> ANGUIRUS_SPAWN_EGG = ITEMS.register("anguirus_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.ANGUIRUS, 0x8C7A4F, 0x3A3022, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MECHAGODZILLA_SPAWN_EGG = ITEMS.register("mechagodzilla_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.MECHAGODZILLA, 0x9EA7B0, 0xE03030, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> GODZILLA_JUNIOR_SPAWN_EGG = ITEMS.register("godzilla_junior_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.GODZILLA_JUNIOR, 0x3F5A3A, 0xFFB0A0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> BIOLLANTE_SPAWN_EGG = ITEMS.register("biollante_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.BIOLLANTE, 0x2E5E22, 0xC8202A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> BATTRA_LARVA_SPAWN_EGG = ITEMS.register("battra_larva_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.BATTRA_LARVA, 0x2A1E1A, 0xB0202A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> BATTRA_SPAWN_EGG = ITEMS.register("battra_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.BATTRA, 0x3A1E2A, 0xE0A020, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> SPACE_GODZILLA_SPAWN_EGG = ITEMS.register("space_godzilla_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SPACE_GODZILLA, 0x2A3A6A, 0xD080FF, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> DESTOROYAH_SPAWN_EGG = ITEMS.register("destoroyah_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.DESTOROYAH, 0x7A1A2A, 0x3A2A4A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> DESTOROYAH_JUVENILE_SPAWN_EGG = ITEMS.register("destoroyah_juvenile_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.DESTOROYAH_JUVENILE, 0x5A1A2A, 0xC06070, new Item.Properties()));

    // Phase 3
    public static final DeferredItem<DeferredSpawnEggItem> MONSTERVERSE_GODZILLA_SPAWN_EGG = ITEMS.register("monsterverse_godzilla_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.MONSTERVERSE_GODZILLA, 0x2B3238, 0x58C8FF, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> KONG_SPAWN_EGG = ITEMS.register("kong_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.KONG, 0x3A2E26, 0x8A6A50, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> SUKO_SPAWN_EGG = ITEMS.register("suko_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SUKO, 0x6A5A4A, 0xC8B8A0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> GREAT_APE_SPAWN_EGG = ITEMS.register("great_ape_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.GREAT_APE, 0x5A4030, 0xD0B090, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> SKAR_KING_SPAWN_EGG = ITEMS.register("skar_king_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SKAR_KING, 0xB04A2A, 0x70D0FF, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> SHIMO_SPAWN_EGG = ITEMS.register("shimo_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SHIMO, 0xC8D8E8, 0x60A8E0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MUTO_SPAWN_EGG = ITEMS.register("muto_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.MUTO, 0x2A2A2E, 0xE04030, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> SKULLCRAWLER_SPAWN_EGG = ITEMS.register("skullcrawler_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SKULLCRAWLER, 0x3A3630, 0xE8E0D0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> SKULL_DEVIL_SPAWN_EGG = ITEMS.register("skull_devil_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SKULL_DEVIL, 0x2A2620, 0xD0C8B8, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> WARBAT_SPAWN_EGG = ITEMS.register("warbat_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.WARBAT, 0x3A1A1A, 0xC02020, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MONSTERVERSE_KING_GHIDORAH_SPAWN_EGG = ITEMS.register("monsterverse_king_ghidorah_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.MONSTERVERSE_KING_GHIDORAH, 0xB8901A, 0x60C0FF, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MONSTERVERSE_RODAN_SPAWN_EGG = ITEMS.register("monsterverse_rodan_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.MONSTERVERSE_RODAN, 0x4A2A1A, 0xFF6020, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MONSTERVERSE_MOTHRA_SPAWN_EGG = ITEMS.register("monsterverse_mothra_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.MONSTERVERSE_MOTHRA, 0xE0C060, 0x40A0E0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> SCYLLA_SPAWN_EGG = ITEMS.register("scylla_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SCYLLA, 0x4A4A3A, 0xA0A080, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> BEHEMOTH_SPAWN_EGG = ITEMS.register("behemoth_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.BEHEMOTH, 0x5A4A3A, 0x80A060, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> METHUSELAH_SPAWN_EGG = ITEMS.register("methuselah_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.METHUSELAH, 0x6A6A60, 0x4A7A3A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> TIAMAT_SPAWN_EGG = ITEMS.register("tiamat_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.TIAMAT, 0x1A2A4A, 0xE070C0, new Item.Properties()));

    // Phase 4: Showa and Millennium tech
    public static final DeferredItem<VehicleItem> KIRYU = ITEMS.register("kiryu",
            () -> new VehicleItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), ModEntities.KIRYU, "kiryu"));
    public static final DeferredItem<VehicleItem> GOTENGO = ITEMS.register("gotengo",
            () -> new VehicleItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), ModEntities.GOTENGO, "gotengo"));
    public static final DeferredItem<com.reiko45.godzilla.item.JetJaguarItem> JET_JAGUAR = ITEMS.register("jet_jaguar",
            () -> new com.reiko45.godzilla.item.JetJaguarItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredItem<com.reiko45.godzilla.item.JetJaguarControllerItem> JET_JAGUAR_CONTROLLER = ITEMS.register("jet_jaguar_controller",
            () -> new com.reiko45.godzilla.item.JetJaguarControllerItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<com.reiko45.godzilla.item.DimensionTideItem> DIMENSION_TIDE = ITEMS.register("dimension_tide",
            () -> new com.reiko45.godzilla.item.DimensionTideItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<com.reiko45.godzilla.item.D03MissileItem> D03_MISSILE = ITEMS.register("d03_missile",
            () -> new com.reiko45.godzilla.item.D03MissileItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
    public static final DeferredItem<com.reiko45.godzilla.item.ThrowableKaijuItem> LIGHT_BOMB = ITEMS.register("light_bomb",
            () -> new com.reiko45.godzilla.item.ThrowableKaijuItem(new Item.Properties().stacksTo(16),
                    (level, player) -> new com.reiko45.godzilla.entity.projectile.LightBombEntity(level, player), 1.0f, 10, "light_bomb"));
    public static final DeferredItem<com.reiko45.godzilla.item.ThrowableKaijuItem> BLADED_SLICER = ITEMS.register("bladed_slicer",
            () -> new com.reiko45.godzilla.item.ThrowableKaijuItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE),
                    (level, player) -> {
                        var disc = new com.reiko45.godzilla.entity.projectile.SlicerDiscEntity(level, player);
                        disc.setTarget(com.reiko45.godzilla.item.ThrowableKaijuItem.lookTarget(player, 64));
                        return disc;
                    }, 1.3f, 20, "bladed_slicer"));
    public static final DeferredItem<com.reiko45.godzilla.item.SupersonicOscillatorItem> SUPERSONIC_OSCILLATOR = ITEMS.register("supersonic_oscillator",
            () -> new com.reiko45.godzilla.item.SupersonicOscillatorItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredItem<com.reiko45.godzilla.item.YellowJuiceItem> YELLOW_JUICE = ITEMS.register("yellow_juice",
            () -> new com.reiko45.godzilla.item.YellowJuiceItem(new Item.Properties().stacksTo(16), false));
    public static final DeferredItem<com.reiko45.godzilla.item.YellowJuiceItem> FAKE_YELLOW_JUICE = ITEMS.register("fake_yellow_juice",
            () -> new com.reiko45.godzilla.item.YellowJuiceItem(new Item.Properties().stacksTo(16), true));
    public static final DeferredItem<LoreItem> ORGANIZER_G1 = ITEMS.register("organizer_g1",
            () -> new LoreItem(new Item.Properties().rarity(Rarity.EPIC).food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(2)
                    .saturationModifier(0.5f).alwaysEdible()
                    .effect(() -> new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 600, 2), 1f)
                    .effect(() -> new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION, 1200, 1), 1f).build()), "organizer_g1"));
    public static final DeferredItem<LoreItem> WHITE_HERON_CELL = ITEMS.register("white_heron_cell",
            () -> new LoreItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON), "white_heron_cell"));
    public static final DeferredItem<LoreItem> KIRYU_KAI_KIT = ITEMS.register("kiryu_kai_kit",
            () -> new LoreItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), "kiryu_kai_kit"));
    public static final DeferredItem<LoreItem> GODZILLA_BONES = ITEMS.register("godzilla_bones",
            () -> new LoreItem(new Item.Properties().rarity(Rarity.EPIC).fireResistant(), "godzilla_bones"));
    public static final DeferredItem<net.minecraft.world.item.SwordItem> EBIRAH_CLAW = ITEMS.register("ebirah_claw",
            () -> new net.minecraft.world.item.SwordItem(net.minecraft.world.item.Tiers.DIAMOND, new Item.Properties().rarity(Rarity.RARE)
                    .attributes(net.minecraft.world.item.SwordItem.createAttributes(net.minecraft.world.item.Tiers.DIAMOND, 5, -2.8f))));
    public static final DeferredItem<net.minecraft.world.item.SwordItem> KAMACURAS_SCYTHE = ITEMS.register("kamacuras_scythe",
            () -> new net.minecraft.world.item.SwordItem(net.minecraft.world.item.Tiers.IRON, new Item.Properties().rarity(Rarity.UNCOMMON)
                    .attributes(net.minecraft.world.item.SwordItem.createAttributes(net.minecraft.world.item.Tiers.IRON, 4, -2.2f))));

    public static final DeferredItem<BlockItem> ALIEN_TRANSMITTER = ITEMS.registerSimpleBlockItem(ModBlocks.ALIEN_TRANSMITTER, new Item.Properties().rarity(Rarity.EPIC));
    public static final DeferredItem<BlockItem> ELECTRODE_PLATE = ITEMS.registerSimpleBlockItem(ModBlocks.ELECTRODE_PLATE, new Item.Properties());
    public static final DeferredItem<BlockItem> MINILLA_EGG = ITEMS.registerSimpleBlockItem(ModBlocks.MINILLA_EGG, new Item.Properties().rarity(Rarity.EPIC));
    public static final DeferredItem<BlockItem> SHISA_STATUE = ITEMS.registerSimpleBlockItem(ModBlocks.SHISA_STATUE, new Item.Properties().rarity(Rarity.EPIC));
    public static final DeferredItem<BlockItem> GUARDIAN_SHRINE = ITEMS.registerSimpleBlockItem(ModBlocks.GUARDIAN_SHRINE, new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<BlockItem> MEGANULON_EGG = ITEMS.registerSimpleBlockItem(ModBlocks.MEGANULON_EGG, new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<BlockItem> KIRYU_CHARGER = ITEMS.registerSimpleBlockItem(ModBlocks.KIRYU_CHARGER, new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<BlockItem> TOWER_LASER = ITEMS.registerSimpleBlockItem(ModBlocks.TOWER_LASER, new Item.Properties().rarity(Rarity.RARE));

    // Phase 4 spawn eggs
    public static final DeferredItem<DeferredSpawnEggItem> GIGAN_SPAWN_EGG = ITEMS.register("gigan_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.GIGAN, 0x5A3A6A, 0xE04040, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> HEDORAH_SPAWN_EGG = ITEMS.register("hedorah_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.HEDORAH, 0x3A3A30, 0xD03030, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MEGALON_SPAWN_EGG = ITEMS.register("megalon_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.MEGALON, 0x3A5A3A, 0xE0C040, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> EBIRAH_SPAWN_EGG = ITEMS.register("ebirah_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.EBIRAH, 0xB04030, 0x30A0A0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> KUMONGA_SPAWN_EGG = ITEMS.register("kumonga_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.KUMONGA, 0x4A4A3A, 0x80C0FF, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> KAMACURAS_SPAWN_EGG = ITEMS.register("kamacuras_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.KAMACURAS, 0x5A7A3A, 0xC0D080, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MINILLA_SPAWN_EGG = ITEMS.register("minilla_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.MINILLA, 0x6A7A5A, 0xE0E0C0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> GOROSAURUS_SPAWN_EGG = ITEMS.register("gorosaurus_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.GOROSAURUS, 0x7A5A3A, 0xD0A060, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MANDA_SPAWN_EGG = ITEMS.register("manda_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.MANDA, 0x3A5A4A, 0xC0C0A0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> BARAGON_SPAWN_EGG = ITEMS.register("baragon_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.BARAGON, 0xB06A3A, 0x3A2A20, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> VARAN_SPAWN_EGG = ITEMS.register("varan_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.VARAN, 0x5A6A6A, 0xA0B0A0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> KING_CAESAR_SPAWN_EGG = ITEMS.register("king_caesar_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.KING_CAESAR, 0xD0A060, 0xFFFFFF, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> TITANOSAURUS_SPAWN_EGG = ITEMS.register("titanosaurus_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.TITANOSAURUS, 0xA04A3A, 0x4A6A8A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MILLENNIUM_GODZILLA_SPAWN_EGG = ITEMS.register("millennium_godzilla_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.MILLENNIUM_GODZILLA, 0x3A3A44, 0xFF6040, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> ORGA_SPAWN_EGG = ITEMS.register("orga_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.ORGA, 0x6A6A50, 0xFFC040, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MILLENNIAN_UFO_SPAWN_EGG = ITEMS.register("millennian_ufo_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.MILLENNIAN_UFO, 0x606870, 0xFFB040, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MEGAGUIRUS_SPAWN_EGG = ITEMS.register("megaguirus_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.MEGAGUIRUS, 0x4A6A2A, 0xE0E060, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MEGANULON_SPAWN_EGG = ITEMS.register("meganulon_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.MEGANULON, 0x5A5A3A, 0x90A060, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MEGANULA_SPAWN_EGG = ITEMS.register("meganula_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.MEGANULA, 0x4A6A3A, 0xC0E080, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> GMK_MOTHRA_SPAWN_EGG = ITEMS.register("gmk_mothra_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.GMK_MOTHRA, 0x8A6A30, 0xE0E0E0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> GMK_KING_GHIDORAH_SPAWN_EGG = ITEMS.register("gmk_king_ghidorah_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.GMK_KING_GHIDORAH, 0xC0A040, 0x40A040, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MONSTER_X_SPAWN_EGG = ITEMS.register("monster_x_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.MONSTER_X, 0xC0B8A0, 0x6A3A8A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> ZILLA_SPAWN_EGG = ITEMS.register("zilla_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.ZILLA, 0x6A6A50, 0x3A3A30, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> XILIEN_SAUCER_SPAWN_EGG = ITEMS.register("xilien_saucer_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.XILIEN_SAUCER, 0x707880, 0xFF3030, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> GIGAN_FINAL_WARS_SPAWN_EGG = variantEgg("gigan_final_wars", ModEntities.GIGAN, 0x3A2A4A, 0xFF2020, 1);
    public static final DeferredItem<DeferredSpawnEggItem> HEDORAH_PERFECT_SPAWN_EGG = variantEgg("hedorah_perfect", ModEntities.HEDORAH, 0x2A2A20, 0xFF4040, 3);
    public static final DeferredItem<DeferredSpawnEggItem> EBIRAH_FINAL_WARS_SPAWN_EGG = variantEgg("ebirah_final_wars", ModEntities.EBIRAH, 0x8A3020, 0x20C0C0, 1);
    public static final DeferredItem<DeferredSpawnEggItem> KUMONGA_FINAL_WARS_SPAWN_EGG = variantEgg("kumonga_final_wars", ModEntities.KUMONGA, 0x3A3A2A, 0xFF4040, 1);
    public static final DeferredItem<DeferredSpawnEggItem> KAMACURAS_FINAL_WARS_SPAWN_EGG = variantEgg("kamacuras_final_wars", ModEntities.KAMACURAS, 0x4A6A2A, 0xFF8040, 1);
    public static final DeferredItem<DeferredSpawnEggItem> MANDA_FINAL_WARS_SPAWN_EGG = variantEgg("manda_final_wars", ModEntities.MANDA, 0x2A4A3A, 0xFF4040, 1);
    public static final DeferredItem<DeferredSpawnEggItem> BARAGON_GMK_SPAWN_EGG = variantEgg("baragon_gmk", ModEntities.BARAGON, 0xC07A40, 0xFFE0A0, 1);
    public static final DeferredItem<DeferredSpawnEggItem> KING_CAESAR_FINAL_WARS_SPAWN_EGG = variantEgg("king_caesar_final_wars", ModEntities.KING_CAESAR, 0xB08040, 0xFF4040, 1);
    public static final DeferredItem<DeferredSpawnEggItem> MECHAGODZILLA_2_SPAWN_EGG = variantEgg("mechagodzilla_2", ModEntities.MECHAGODZILLA, 0x8A9098, 0xFFD040, 1);
    public static final DeferredItem<DeferredSpawnEggItem> GODZILLA_GMK_SPAWN_EGG = variantEgg("godzilla_gmk", ModEntities.MILLENNIUM_GODZILLA, 0x2A2A2E, 0xFFFFFF, 1);
    public static final DeferredItem<DeferredSpawnEggItem> GODZILLA_KIRYU_ERA_SPAWN_EGG = variantEgg("godzilla_kiryu_era", ModEntities.MILLENNIUM_GODZILLA, 0x2E3A3A, 0x60C0FF, 2);
    public static final DeferredItem<DeferredSpawnEggItem> GODZILLA_FINAL_WARS_SPAWN_EGG = variantEgg("godzilla_final_wars", ModEntities.MILLENNIUM_GODZILLA, 0x2A2A30, 0x40A0FF, 3);

    // Phase 5: Reiwa and modern
    public static final DeferredItem<VehicleItem> SHINDEN = ITEMS.register("shinden",
            () -> new VehicleItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), ModEntities.SHINDEN, "shinden"));
    public static final DeferredItem<VehicleItem> VULTURE = ITEMS.register("vulture",
            () -> new VehicleItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), ModEntities.VULTURE, "vulture"));
    public static final DeferredItem<VehicleItem> JET_JAGUAR_PP = ITEMS.register("jet_jaguar_pp",
            () -> new VehicleItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), ModEntities.JET_JAGUAR_PP, "jet_jaguar_pp"));
    public static final DeferredItem<com.reiko45.godzilla.item.TechItem> COAGULANT_PUMP = ITEMS.register("coagulant_pump",
            () -> new com.reiko45.godzilla.item.TechItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), com.reiko45.godzilla.item.TechItem.Kind.COAGULANT_PUMP));
    public static final DeferredItem<com.reiko45.godzilla.item.TechItem> TRAIN_BOMB = ITEMS.register("train_bomb",
            () -> new com.reiko45.godzilla.item.TechItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON), com.reiko45.godzilla.item.TechItem.Kind.TRAIN_BOMB));
    public static final DeferredItem<com.reiko45.godzilla.item.TechItem> DRONE_SWARM = ITEMS.register("drone_swarm",
            () -> new com.reiko45.godzilla.item.TechItem(new Item.Properties().stacksTo(16), com.reiko45.godzilla.item.TechItem.Kind.DRONE_SWARM));
    public static final DeferredItem<com.reiko45.godzilla.item.TechItem> MOP_DESIGNATOR = ITEMS.register("mop_designator",
            () -> new com.reiko45.godzilla.item.TechItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), com.reiko45.godzilla.item.TechItem.Kind.MOP_DESIGNATOR));
    public static final DeferredItem<com.reiko45.godzilla.item.TechItem> FREON_TANK = ITEMS.register("freon_tank",
            () -> new com.reiko45.godzilla.item.TechItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON), com.reiko45.godzilla.item.TechItem.Kind.FREON_TANK));
    public static final DeferredItem<com.reiko45.godzilla.item.TechItem> BALLOON_RIG = ITEMS.register("balloon_rig",
            () -> new com.reiko45.godzilla.item.TechItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON), com.reiko45.godzilla.item.TechItem.Kind.BALLOON_RIG));
    public static final DeferredItem<com.reiko45.godzilla.item.TechItem> RADIATION_BUOY = ITEMS.register("radiation_buoy",
            () -> new com.reiko45.godzilla.item.TechItem(new Item.Properties().stacksTo(1), com.reiko45.godzilla.item.TechItem.Kind.RADIATION_BUOY));
    public static final DeferredItem<com.reiko45.godzilla.item.TechItem> GRAVITY_GLAND = ITEMS.register("gravity_gland",
            () -> new com.reiko45.godzilla.item.TechItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), com.reiko45.godzilla.item.TechItem.Kind.GRAVITY_GLAND));
    public static final DeferredItem<com.reiko45.godzilla.item.TechItem> EMP_PROBE = ITEMS.register("emp_probe",
            () -> new com.reiko45.godzilla.item.TechItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON), com.reiko45.godzilla.item.TechItem.Kind.EMP_PROBE));
    public static final DeferredItem<com.reiko45.godzilla.item.TechItem> ORTHOGONAL_DIAGONALIZER = ITEMS.register("orthogonal_diagonalizer",
            () -> new com.reiko45.godzilla.item.TechItem(new Item.Properties().durability(32).rarity(Rarity.EPIC), com.reiko45.godzilla.item.TechItem.Kind.DIAGONALIZER));
    public static final DeferredItem<LoreItem> COAGULANT_CANISTER = ITEMS.register("coagulant_canister",
            () -> new LoreItem(new Item.Properties().stacksTo(64), "coagulant_canister"));
    public static final DeferredItem<LoreItem> SHIN_TISSUE = ITEMS.register("shin_tissue",
            () -> new LoreItem(new Item.Properties().rarity(Rarity.RARE), "shin_tissue"));
    public static final DeferredItem<LoreItem> CONTAMINATED_BLOOD = ITEMS.register("contaminated_blood",
            () -> new LoreItem(new Item.Properties().rarity(Rarity.UNCOMMON), "contaminated_blood"));
    public static final DeferredItem<LoreItem> REGENERATING_TISSUE = ITEMS.register("regenerating_tissue",
            () -> new LoreItem(new Item.Properties().rarity(Rarity.EPIC).food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(3)
                    .saturationModifier(0.4f).alwaysEdible()
                    .effect(() -> new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 400, 3), 1f)
                    .effect(() -> new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.CONFUSION, 200, 0), 1f).build()), "regenerating_tissue"));
    public static final DeferredItem<LoreItem> NANOMETAL_INGOT = ITEMS.register("nanometal_ingot",
            () -> new LoreItem(new Item.Properties(), "nanometal_ingot"));
    public static final DeferredItem<LoreItem> METALLIC_FIBER = ITEMS.register("metallic_fiber",
            () -> new LoreItem(new Item.Properties(), "metallic_fiber"));
    public static final DeferredItem<LoreItem> ARCHETYPE_CRYSTAL = ITEMS.register("archetype_crystal",
            () -> new LoreItem(new Item.Properties().rarity(Rarity.UNCOMMON), "archetype_crystal"));
    public static final DeferredItem<LoreItem> DIAGONALIZER_CODE = ITEMS.register("diagonalizer_code",
            () -> new LoreItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), "diagonalizer_code"));
    public static final DeferredItem<LoreItem> HOUTUA_SCALE = ITEMS.register("houtua_scale",
            () -> new LoreItem(new Item.Properties().rarity(Rarity.RARE), "houtua_scale"));
    public static final DeferredItem<LoreItem> GARBETRIUM_SHARD = ITEMS.register("garbetrium_shard",
            () -> new LoreItem(new Item.Properties().rarity(Rarity.RARE), "garbetrium_shard"));
    public static final DeferredItem<net.minecraft.world.item.SwordItem> NANOMETAL_BLADE = ITEMS.register("nanometal_blade",
            () -> new net.minecraft.world.item.SwordItem(net.minecraft.world.item.Tiers.DIAMOND, new Item.Properties().rarity(Rarity.RARE)
                    .attributes(net.minecraft.world.item.SwordItem.createAttributes(net.minecraft.world.item.Tiers.DIAMOND, 4, -2.4f))));
    public static final DeferredItem<BlockItem> FALLOUT_ASH = ITEMS.registerSimpleBlockItem(ModBlocks.FALLOUT_ASH, new Item.Properties());
    public static final DeferredItem<BlockItem> RED_DUST = ITEMS.registerSimpleBlockItem(ModBlocks.RED_DUST, new Item.Properties());
    public static final DeferredItem<BlockItem> NANOMETAL = ITEMS.registerSimpleBlockItem(ModBlocks.NANOMETAL, new Item.Properties());
    public static final DeferredItem<BlockItem> LIQUID_NANOMETAL = ITEMS.registerSimpleBlockItem(ModBlocks.LIQUID_NANOMETAL, new Item.Properties());
    public static final DeferredItem<BlockItem> STORM_BEACON = ITEMS.registerSimpleBlockItem(ModBlocks.STORM_BEACON, new Item.Properties());
    public static final DeferredItem<BlockItem> DEMOLITION_CHARGE = ITEMS.registerSimpleBlockItem(ModBlocks.DEMOLITION_CHARGE, new Item.Properties());
    public static final DeferredItem<BlockItem> METALLIC_FROND = ITEMS.registerSimpleBlockItem(ModBlocks.METALLIC_FROND, new Item.Properties());
    public static final DeferredItem<BlockItem> ARCHETYPE_CRYSTAL_CLUSTER = ITEMS.registerSimpleBlockItem(ModBlocks.ARCHETYPE_CRYSTAL, new Item.Properties());
    public static final DeferredItem<BlockItem> GARBETRIUM_TOTEM = ITEMS.registerSimpleBlockItem(ModBlocks.GARBETRIUM_TOTEM, new Item.Properties().rarity(Rarity.EPIC));
    public static final DeferredItem<BlockItem> TITAN_X_EGG = ITEMS.registerSimpleBlockItem(ModBlocks.TITAN_X_EGG, new Item.Properties().rarity(Rarity.EPIC));

    // Phase 5 spawn eggs
    public static final DeferredItem<DeferredSpawnEggItem> SHIN_GODZILLA_2_SPAWN_EGG = ITEMS.register("shin_godzilla_2_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SHIN_GODZILLA, 0x6A4A3A, 0xE07A6A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> SHIN_GODZILLA_3_SPAWN_EGG = variantEgg("shin_godzilla_3", ModEntities.SHIN_GODZILLA, 0x7A3A3A, 0xFF5040, 1);
    public static final DeferredItem<DeferredSpawnEggItem> SHIN_GODZILLA_SPAWN_EGG = variantEgg("shin_godzilla", ModEntities.SHIN_GODZILLA, 0x3A2A2A, 0xC060FF, 2);
    public static final DeferredItem<DeferredSpawnEggItem> GODZILLA_ODO_SPAWN_EGG = ITEMS.register("godzilla_odo_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.MINUS_ONE_GODZILLA, 0x3A3A34, 0xA0A090, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> GODZILLA_MINUS_ONE_SPAWN_EGG = variantEgg("godzilla_minus_one", ModEntities.MINUS_ONE_GODZILLA, 0x2E3034, 0x80C0FF, 1);
    public static final DeferredItem<DeferredSpawnEggItem> GODZILLA_MINUS_ZERO_SPAWN_EGG = variantEgg("godzilla_minus_zero", ModEntities.MINUS_ONE_GODZILLA, 0x262628, 0x40A0FF, 2);
    public static final DeferredItem<DeferredSpawnEggItem> GHIDORAH_MINUS_ZERO_SPAWN_EGG = ITEMS.register("ghidorah_minus_zero_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.MINUS_ZERO_GHIDORAH, 0xC09030, 0xFF8020, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> KING_GHIDORAH_MINUS_ZERO_SPAWN_EGG = variantEgg("king_ghidorah_minus_zero", ModEntities.MINUS_ZERO_GHIDORAH, 0xD0A040, 0xFF6010, 1);
    public static final DeferredItem<DeferredSpawnEggItem> GODZILLA_FILIUS_SPAWN_EGG = ITEMS.register("godzilla_filius_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.GODZILLA_EARTH, 0x3A4038, 0x80E0FF, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> GODZILLA_EARTH_SPAWN_EGG = variantEgg("godzilla_earth", ModEntities.GODZILLA_EARTH, 0x2E3A30, 0x60C0FF, 1);
    public static final DeferredItem<DeferredSpawnEggItem> SERVUM_SPAWN_EGG = ITEMS.register("servum_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SERVUM, 0x5A5A60, 0xC0C0D0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> SERVUM_WORM_SPAWN_EGG = variantEgg("servum_worm", ModEntities.SERVUM, 0x4A4A50, 0xA08060, 1);
    public static final DeferredItem<DeferredSpawnEggItem> MECHAGODZILLA_CITY_SPAWN_EGG = ITEMS.register("mechagodzilla_city_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.MECHAGODZILLA_CITY, 0x8090A0, 0x40E0FF, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> PLANET_EATER_GHIDORAH_SPAWN_EGG = ITEMS.register("planet_eater_ghidorah_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.PLANET_EATER_GHIDORAH, 0xE0C060, 0xFFFFE0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> GODZILLA_AQUATILIS_SPAWN_EGG = ITEMS.register("godzilla_aquatilis_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SINGULAR_GODZILLA, 0xD06030, 0xFFC080, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> GODZILLA_TERRESTRIS_SPAWN_EGG = variantEgg("godzilla_terrestris", ModEntities.SINGULAR_GODZILLA, 0x6A3A2A, 0x60A0FF, 2);
    public static final DeferredItem<DeferredSpawnEggItem> GODZILLA_ULTIMA_SPAWN_EGG = variantEgg("godzilla_ultima", ModEntities.SINGULAR_GODZILLA, 0x1E1E22, 0xFF5030, 3);
    public static final DeferredItem<DeferredSpawnEggItem> RODAN_SP_SPAWN_EGG = ITEMS.register("rodan_sp_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SINGULAR_RODAN, 0xA04030, 0x302020, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> RODAN_SP_ADULT_SPAWN_EGG = variantEgg("rodan_sp_adult", ModEntities.SINGULAR_RODAN, 0xB05030, 0x502020, 1);
    public static final DeferredItem<DeferredSpawnEggItem> SALUNGA_SPAWN_EGG = ITEMS.register("salunga_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SALUNGA, 0x6A5A4A, 0x80C0FF, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> TITAN_X_SPAWN_EGG = ITEMS.register("titan_x_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.TITAN_X, 0x2A3A5A, 0x60FFF0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> TITAN_X_IMPLANTED_SPAWN_EGG = variantEgg("titan_x_implanted", ModEntities.TITAN_X, 0x2A2A4A, 0xFF4040, 1);
    public static final DeferredItem<DeferredSpawnEggItem> SCARAB_SPAWN_EGG = ITEMS.register("scarab_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SCARAB, 0x203040, 0x80FFF0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> FROST_VARK_SPAWN_EGG = ITEMS.register("frost_vark_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.FROST_VARK, 0x8A8A9A, 0xD0F0FF, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> ION_DRAGON_SPAWN_EGG = ITEMS.register("ion_dragon_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.ION_DRAGON, 0x3A5A6A, 0x60FFD0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> KRAKEN_SPAWN_EGG = ITEMS.register("kraken_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.KRAKEN, 0x6A3A4A, 0xD08090, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> GIGAN_REX_SPAWN_EGG = variantEgg("gigan_rex", ModEntities.GIGAN, 0x8A1A2A, 0xFF6060, 2);
    public static final DeferredItem<DeferredSpawnEggItem> GIGAN_MILES_SPAWN_EGG = variantEgg("gigan_miles", ModEntities.GIGAN, 0x4A4A5A, 0x60A0FF, 3);
    public static final DeferredItem<DeferredSpawnEggItem> SPACE_GODZILLA_MV_SPAWN_EGG = variantEgg("space_godzilla_mv", ModEntities.SPACE_GODZILLA, 0x3A3A6A, 0x80E0FF, 1);

    /** A spawn egg for one of an entity's other forms (Final Wars, GMK...): it carries the variant in its entity data. */
    private static <T extends net.minecraft.world.entity.Mob> DeferredItem<DeferredSpawnEggItem> variantEgg(String name,
            java.util.function.Supplier<? extends net.minecraft.world.entity.EntityType<T>> type, int background, int highlight, int variant) {
        return ITEMS.register(name + "_spawn_egg", () -> {
            net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
            tag.putString("id", net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(type.get()).toString());
            tag.putInt("Variant", variant);
            tag.putBoolean("SpawnFull", true);
            return new DeferredSpawnEggItem(type, background, highlight, new Item.Properties()
                    .component(net.minecraft.core.component.DataComponents.ENTITY_DATA, net.minecraft.world.item.component.CustomData.of(tag)));
        });
    }

    private ModItems() {}
}

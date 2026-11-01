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
    public static final DeferredItem<DeferredSpawnEggItem> GODZILLA_SPAWN_EGG = ITEMS.register("godzilla_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.GODZILLA, 0x2F3B2F, 0x7FD8FF, new Item.Properties()));
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

    private ModItems() {}
}

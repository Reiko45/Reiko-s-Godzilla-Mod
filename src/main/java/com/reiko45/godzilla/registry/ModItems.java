package com.reiko45.godzilla.registry;

import com.reiko45.godzilla.ReikoGodzilla;
import com.reiko45.godzilla.item.LoreItem;
import com.reiko45.godzilla.item.MaserTankItem;
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
    public static final DeferredItem<MaserTankItem> MASER_TANK = ITEMS.register("maser_tank",
            () -> new MaserTankItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

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

    private ModItems() {}
}

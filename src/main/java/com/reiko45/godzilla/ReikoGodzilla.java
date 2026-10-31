package com.reiko45.godzilla;

import com.mojang.logging.LogUtils;
import com.reiko45.godzilla.network.ModNetwork;
import com.reiko45.godzilla.registry.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(ReikoGodzilla.MODID)
public class ReikoGodzilla {
    public static final String MODID = "reikogodzilla";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ReikoGodzilla(IEventBus modBus, ModContainer container) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModStructures.STRUCTURE_TYPES.register(modBus);
        ModStructures.PIECE_TYPES.register(modBus);

        modBus.addListener(ModEntities::registerAttributes);
        modBus.addListener(ModNetwork::register);

        container.registerConfig(ModConfig.Type.COMMON, GodzillaConfig.SPEC);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}

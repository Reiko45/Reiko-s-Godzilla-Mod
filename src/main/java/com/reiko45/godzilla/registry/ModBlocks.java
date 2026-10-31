package com.reiko45.godzilla.registry;

import com.reiko45.godzilla.ReikoGodzilla;
import com.reiko45.godzilla.block.GCellRoseBlock;
import com.reiko45.godzilla.block.GodzillaEggBlock;
import com.reiko45.godzilla.block.KaijuLairBlock;
import com.reiko45.godzilla.block.MothraEggBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ReikoGodzilla.MODID);

    public static final DeferredBlock<MothraEggBlock> MOTHRA_EGG = BLOCKS.register("mothra_egg",
            () -> new MothraEggBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(3.0f, 6.0f)
                    .sound(SoundType.BONE_BLOCK).randomTicks().noOcclusion().pushReaction(PushReaction.BLOCK)));

    /** Invisible marker placed by island structures; spawns its kaiju when a player comes near, then disappears. */
    public static final DeferredBlock<KaijuLairBlock> KAIJU_LAIR = BLOCKS.register("kaiju_lair",
            () -> new KaijuLairBlock(BlockBehaviour.Properties.of().strength(-1.0f, 3_600_000.0f).noCollission().noLootTable()
                    .noOcclusion()));

    public static final DeferredBlock<GodzillaEggBlock> GODZILLA_EGG = BLOCKS.register("godzilla_egg",
            () -> new GodzillaEggBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_GREEN).strength(3.0f, 6.0f)
                    .sound(SoundType.BONE_BLOCK).randomTicks().noOcclusion().pushReaction(PushReaction.BLOCK)));

    public static final DeferredBlock<GCellRoseBlock> G_CELL_ROSE = BLOCKS.register("g_cell_rose",
            () -> new GCellRoseBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).noCollission().instabreak()
                    .sound(SoundType.GRASS).randomTicks().lightLevel(s -> 5).pushReaction(PushReaction.DESTROY)));

    private ModBlocks() {}
}

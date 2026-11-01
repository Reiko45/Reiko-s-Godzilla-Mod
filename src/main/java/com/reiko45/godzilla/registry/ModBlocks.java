package com.reiko45.godzilla.registry;

import com.reiko45.godzilla.ReikoGodzilla;
import com.reiko45.godzilla.block.GCellRoseBlock;
import com.reiko45.godzilla.block.GodzillaEggBlock;
import com.reiko45.godzilla.block.KaijuLairBlock;
import com.reiko45.godzilla.block.MothraEggBlock;
import com.reiko45.godzilla.block.*;
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

    // Phase 3
    public static final DeferredBlock<NuclearWarheadBlock> NUCLEAR_WARHEAD = BLOCKS.register("nuclear_warhead",
            () -> new NuclearWarheadBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0f, 1200.0f)
                    .sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops().noOcclusion().lightLevel(s -> 3)));
    public static final DeferredBlock<SolarCrystalBlock> SOLAR_CRYSTAL = BLOCKS.register("solar_crystal",
            () -> new SolarCrystalBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(4.0f, 6.0f)
                    .sound(SoundType.AMETHYST).lightLevel(s -> 15).noOcclusion()));
    public static final DeferredBlock<MutoEggBlock> MUTO_EGG = BLOCKS.register("muto_egg",
            () -> new MutoEggBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(1.5f, 2.0f)
                    .sound(SoundType.FROGSPAWN).randomTicks().noOcclusion().lightLevel(s -> 6).pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<OrcaBlock> ORCA = BLOCKS.register("orca",
            () -> new OrcaBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f, 6.0f).sound(SoundType.METAL)
                    .requiresCorrectToolForDrops().lightLevel(s -> OrcaBlock.isBroadcasting(s) ? 10 : 2)));
    public static final DeferredBlock<HollowEarthVortexBlock> HOLLOW_EARTH_VORTEX = BLOCKS.register("hollow_earth_vortex",
            () -> new HollowEarthVortexBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(-1.0f, 3_600_000.0f)
                    .noCollission().noLootTable().noOcclusion().lightLevel(s -> 12).pushReaction(PushReaction.BLOCK)));
    /** Invisible ticking marker at the centre of Skull Island. */
    public static final DeferredBlock<StormWallBlock> STORM_WALL = BLOCKS.register("storm_wall",
            () -> new StormWallBlock(BlockBehaviour.Properties.of().strength(-1.0f, 3_600_000.0f).noCollission().noLootTable().noOcclusion()));

    private ModBlocks() {}
}

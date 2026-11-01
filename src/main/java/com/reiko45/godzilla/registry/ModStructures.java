package com.reiko45.godzilla.registry;

import com.reiko45.godzilla.ReikoGodzilla;
import com.reiko45.godzilla.world.KaijuIslandPiece;
import com.reiko45.godzilla.world.KaijuIslandStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, ReikoGodzilla.MODID);
    public static final DeferredRegister<StructurePieceType> PIECE_TYPES = DeferredRegister.create(Registries.STRUCTURE_PIECE, ReikoGodzilla.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<KaijuIslandStructure>> KAIJU_ISLAND =
            STRUCTURE_TYPES.register("kaiju_island", () -> () -> KaijuIslandStructure.CODEC);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> KAIJU_ISLAND_PIECE =
            PIECE_TYPES.register("kaiju_island", () -> (StructurePieceType.ContextlessType) KaijuIslandPiece::new);

    public static final DeferredHolder<StructureType<?>, StructureType<com.reiko45.godzilla.world.MonsterverseSiteStructure>> MONSTERVERSE_SITE =
            STRUCTURE_TYPES.register("monsterverse_site", () -> () -> com.reiko45.godzilla.world.MonsterverseSiteStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> MONSTERVERSE_SITE_PIECE =
            PIECE_TYPES.register("monsterverse_site", () -> (StructurePieceType.ContextlessType) com.reiko45.godzilla.world.MonsterverseSitePiece::new);

    private ModStructures() {}
}

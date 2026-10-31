package com.reiko45.godzilla.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.reiko45.godzilla.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * Islands raised out of open ocean: Monster Island (volcano, Godzilla/Rodan/Anguirus lairs) and Infant Island
 * (jungle shrine with Mothra's egg). The "island" field in the structure JSON picks which.
 */
public class KaijuIslandStructure extends Structure {
    public static final MapCodec<KaijuIslandStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance),
            Codec.STRING.fieldOf("island").forGetter(s -> s.island.getSerializedName())
    ).apply(instance, (settings, name) -> new KaijuIslandStructure(settings, IslandKind.byName(name))));

    private final IslandKind island;

    public KaijuIslandStructure(StructureSettings settings, IslandKind island) {
        super(settings);
        this.island = island;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int x = context.chunkPos().getMiddleBlockX();
        int z = context.chunkPos().getMiddleBlockZ();
        int sea = context.chunkGenerator().getSeaLevel();
        int radius = island.radius;
        // Needs open water all around: check the centre and the rim.
        int[][] probes = {{0, 0}, {radius, 0}, {-radius, 0}, {0, radius}, {0, -radius}};
        int floor = Integer.MAX_VALUE;
        for (int[] p : probes) {
            int h = context.chunkGenerator().getFirstOccupiedHeight(x + p[0], z + p[1], Heightmap.Types.OCEAN_FLOOR_WG,
                    context.heightAccessor(), context.randomState());
            if (h > sea - 6) return Optional.empty();
            floor = Math.min(floor, h);
        }
        long seed = context.random().nextLong();
        BlockPos center = new BlockPos(x, sea, z);
        return Optional.of(new GenerationStub(center, builder -> builder.addPiece(new KaijuIslandPiece(island, center, seed))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.KAIJU_ISLAND.get();
    }
}

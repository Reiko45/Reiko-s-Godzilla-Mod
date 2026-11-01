package com.reiko45.godzilla.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.reiko45.godzilla.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/** Finds a spot for a MonsterVerse site: open land, a deep sea floor, or a cavern floor in the Hollow Earth. */
public class MonsterverseSiteStructure extends Structure {
    public static final MapCodec<MonsterverseSiteStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance),
            Codec.STRING.fieldOf("site").forGetter(s -> s.kind.getSerializedName())
    ).apply(instance, (settings, name) -> new MonsterverseSiteStructure(settings, SiteKind.byName(name))));

    private final SiteKind kind;

    public MonsterverseSiteStructure(StructureSettings settings, SiteKind kind) {
        super(settings);
        this.kind = kind;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int x = context.chunkPos().getMiddleBlockX();
        int z = context.chunkPos().getMiddleBlockZ();
        var gen = context.chunkGenerator();
        var heights = context.heightAccessor();
        var rs = context.randomState();
        int sea = gen.getSeaLevel();
        int y;
        if (kind.underground) {
            NoiseColumn column = gen.getBaseColumn(x, z, heights, rs);
            y = findCavernFloor(column, Math.max(sea + 2, heights.getMinBuildHeight() + 8), Math.min(heights.getMaxBuildHeight() - 12, 104), Math.min(kind.headroom, 10));
            if (y == Integer.MIN_VALUE) return Optional.empty();
        } else if (kind == SiteKind.TIAMAT_LAIR) {
            int floor = gen.getFirstOccupiedHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, heights, rs);
            if (sea - floor < 16) return Optional.empty();
            y = floor;
        } else {
            int surface = gen.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, heights, rs);
            int floor = gen.getFirstOccupiedHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, heights, rs);
            if (surface != floor || surface <= sea) return Optional.empty();
            int r = Math.max(4, kind.radius);
            int[][] probes = {{r, 0}, {-r, 0}, {0, r}, {0, -r}};
            for (int[] p : probes) {
                int h = gen.getFirstOccupiedHeight(x + p[0], z + p[1], Heightmap.Types.WORLD_SURFACE_WG, heights, rs);
                if (Math.abs(h - surface) > (kind == SiteKind.VORTEX ? 5 : 12)) return Optional.empty();
            }
            y = surface;
        }
        long seed = context.random().nextLong();
        BlockPos center = new BlockPos(x, y, z);
        return Optional.of(new GenerationStub(center, builder -> builder.addPiece(new MonsterverseSitePiece(kind, center, seed))));
    }

    /** Highest floor between {@code min} and {@code max} with at least {@code headroom} air above it. */
    private static int findCavernFloor(NoiseColumn column, int min, int max, int headroom) {
        for (int y = max; y >= min; y--) {
            BlockState below = column.getBlock(y - 1);
            if (below.isAir() || !below.getFluidState().isEmpty()) continue;
            boolean clear = true;
            for (int dy = 0; dy < headroom && clear; dy++) clear = column.getBlock(y + dy).isAir();
            if (clear) return y;
        }
        return Integer.MIN_VALUE;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.MONSTERVERSE_SITE.get();
    }
}

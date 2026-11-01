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

/** Finds a spot for a Showa/Millennium site: flat open land, a deep sea floor, or a cavern deep underground. */
public class ClassicSiteStructure extends Structure {
    public static final MapCodec<ClassicSiteStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance),
            Codec.STRING.fieldOf("site").forGetter(s -> s.kind.getSerializedName())
    ).apply(instance, (settings, name) -> new ClassicSiteStructure(settings, ClassicSiteKind.byName(name))));

    private final ClassicSiteKind kind;

    public ClassicSiteStructure(StructureSettings settings, ClassicSiteKind kind) {
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
        int surface = gen.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, heights, rs);
        int floor = gen.getFirstOccupiedHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, heights, rs);
        int y;
        switch (kind.placement) {
            case SEA_FLOOR -> {
                if (sea - floor < 16) return Optional.empty();
                y = floor;
            }
            case DEEP -> {
                if (surface != floor || surface <= sea) return Optional.empty();
                y = Math.max(heights.getMinBuildHeight() + 14, Math.min(-28, sea - 40));
            }
            default -> {
                if (surface != floor || surface <= sea) return Optional.empty();
                int r = Math.max(4, kind.radius - 2);
                int[][] probes = {{r, 0}, {-r, 0}, {0, r}, {0, -r}};
                for (int[] p : probes) {
                    int h = gen.getFirstOccupiedHeight(x + p[0], z + p[1], Heightmap.Types.WORLD_SURFACE_WG, heights, rs);
                    int f = gen.getFirstOccupiedHeight(x + p[0], z + p[1], Heightmap.Types.OCEAN_FLOOR_WG, heights, rs);
                    if (h != f || Math.abs(h - surface) > 8) return Optional.empty();
                }
                y = surface;
            }
        }
        long seed = context.random().nextLong();
        BlockPos center = new BlockPos(x, y, z);
        int top = kind.placement == ClassicSiteKind.Placement.DEEP ? surface : y;
        return Optional.of(new GenerationStub(center, builder -> builder.addPiece(new ClassicSitePiece(kind, center, top, seed))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.CLASSIC_SITE.get();
    }
}

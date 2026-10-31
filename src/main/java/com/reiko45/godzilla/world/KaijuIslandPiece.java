package com.reiko45.godzilla.world;

import com.reiko45.godzilla.block.KaijuLairBlockEntity;
import com.reiko45.godzilla.block.MothraEggBlock;
import com.reiko45.godzilla.registry.ModBlocks;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.world.level.ChunkPos;

/**
 * Builds a whole island procedurally: a noisy coastline rising out of the sea floor, beaches, a grassy plateau,
 * trees, and per-island features (Monster Island's volcano and kaiju lairs, Infant Island's shrine and Mothra Egg).
 */
public class KaijuIslandPiece extends StructurePiece {
    private static final int NONE = Integer.MIN_VALUE;

    private final IslandKind kind;
    private final BlockPos center;
    private final long seed;
    private SimplexNoise noise;

    public KaijuIslandPiece(IslandKind kind, BlockPos center, long seed) {
        super(ModStructures.KAIJU_ISLAND_PIECE.get(), 0, makeBox(kind, center));
        this.kind = kind;
        this.center = center;
        this.seed = seed;
    }

    public KaijuIslandPiece(CompoundTag tag) {
        super(ModStructures.KAIJU_ISLAND_PIECE.get(), tag);
        this.kind = IslandKind.byName(tag.getString("Island"));
        this.center = new BlockPos(tag.getInt("CX"), tag.getInt("CY"), tag.getInt("CZ"));
        this.seed = tag.getLong("Seed");
    }

    private static BoundingBox makeBox(IslandKind kind, BlockPos c) {
        int r = (int) (kind.radius * 1.4) + 4;
        return new BoundingBox(c.getX() - r, c.getY() - 64, c.getZ() - r, c.getX() + r, c.getY() + 56, c.getZ() + r);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("Island", kind.getSerializedName());
        tag.putInt("CX", center.getX());
        tag.putInt("CY", center.getY());
        tag.putInt("CZ", center.getZ());
        tag.putLong("Seed", seed);
    }

    private SimplexNoise noise() {
        if (noise == null) noise = new SimplexNoise(new LegacyRandomSource(seed));
        return noise;
    }

    // ------------------------------------------------------------------ shape

    /** Distance from the centre, normalised so the coastline is at 1.0, with a wobbly outline. */
    private double shapeDistance(int x, int z) {
        double dx = x - center.getX(), dz = z - center.getZ();
        double d = Math.sqrt(dx * dx + dz * dz) / kind.radius;
        double angle = Math.atan2(dz, dx);
        double wobble = noise().getValue(Math.cos(angle) * 1.6, Math.sin(angle) * 1.6) * 0.2 + noise().getValue(x * 0.035, z * 0.035) * 0.08;
        return d * (1.0 - wobble);
    }

    private int volcanoX() {
        return center.getX() + (int) (kind.radius * 0.25);
    }

    private int volcanoZ() {
        return center.getZ() - (int) (kind.radius * 0.2);
    }

    private double volcanoRadius() {
        return kind.radius * 0.45;
    }

    /** 0 at the volcano's centre, 1 at its foot (Monster Island only). */
    private double volcanoDistance(int x, int z) {
        if (kind != IslandKind.MONSTER) return 2.0;
        double dx = x - volcanoX(), dz = z - volcanoZ();
        return Math.sqrt(dx * dx + dz * dz) / volcanoRadius() * (1.0 - noise().getValue(x * 0.08, z * 0.08) * 0.08);
    }

    private static final double CRATER = 0.24;
    private static final int VOLCANO_HEIGHT = 34;

    private int craterFloor() {
        return center.getY() + 2 + (int) (VOLCANO_HEIGHT * Math.pow(1 - CRATER, 1.4)) - 9;
    }

    private int lavaLevel() {
        return craterFloor() + 2;
    }

    private boolean inCrater(int x, int z) {
        return volcanoDistance(x, z) < CRATER;
    }

    /** Top block of the island at this column, or NONE if the island doesn't reach here. */
    private int surfaceHeight(int x, int z, int seaFloor) {
        int sea = center.getY();
        double d = shapeDistance(x, z);
        double land = NONE;
        if (d < 1.0) {
            double plateau = kind == IslandKind.MONSTER ? 6 : 9;
            land = sea + 1 + (1 - d * d) * plateau + noise().getValue(x * 0.06 + 100, z * 0.06) * 2.5;
        } else if (d < 1.4) {
            double t = (d - 1.0) / 0.4;
            land = Mth.lerp(t * t, sea - 1, seaFloor);
        }
        double dv = volcanoDistance(x, z);
        if (dv < 1.0) {
            double cone = dv < CRATER ? craterFloor() : sea + 2 + VOLCANO_HEIGHT * Math.pow(1 - dv, 1.4);
            land = Math.max(land, cone);
        }
        return land == NONE ? NONE : (int) Math.floor(land);
    }

    // ------------------------------------------------------------------ generation

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                            BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
        int sea = center.getY();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        RandomSource columnRandom = RandomSource.create(seed ^ chunkPos.toLong());

        for (int x = Math.max(box.minX(), boundingBox.minX()); x <= Math.min(box.maxX(), boundingBox.maxX()); x++) {
            for (int z = Math.max(box.minZ(), boundingBox.minZ()); z <= Math.min(box.maxZ(), boundingBox.maxZ()); z++) {
                int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1;
                int top = surfaceHeight(x, z, floor);
                if (top == NONE || top <= floor) continue;
                boolean volcanic = volcanoDistance(x, z) < 0.9;
                for (int y = floor + 1; y <= top; y++) {
                    level.setBlock(pos.set(x, y, z), pickBlock(top - y, top, sea, volcanic, columnRandom), 2);
                }
                if (inCrater(x, z)) {
                    for (int y = top + 1; y <= lavaLevel(); y++) level.setBlock(pos.set(x, y, z), Blocks.LAVA.defaultBlockState(), 2);
                }
            }
        }

        decorate(level, generator, random, box);
        if (kind == IslandKind.MONSTER) placeMonsterLairs(level, box);
        else buildShrine(level, box);
    }

    private BlockState pickBlock(int depth, int top, int sea, boolean volcanic, RandomSource random) {
        if (volcanic) {
            if (depth == 0) {
                int r = random.nextInt(12);
                return r == 0 ? Blocks.MAGMA_BLOCK.defaultBlockState() : r < 4 ? Blocks.BASALT.defaultBlockState()
                        : r < 7 ? Blocks.TUFF.defaultBlockState() : Blocks.BLACKSTONE.defaultBlockState();
            }
            return depth < 4 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.STONE.defaultBlockState();
        }
        if (top <= sea + 1) {
            return depth < 4 ? Blocks.SAND.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
        }
        if (depth == 0) return Blocks.GRASS_BLOCK.defaultBlockState();
        if (depth < 4) return Blocks.DIRT.defaultBlockState();
        return random.nextInt(20) == 0 ? Blocks.ANDESITE.defaultBlockState() : Blocks.STONE.defaultBlockState();
    }

    private void decorate(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BoundingBox box) {
        int attempts = kind == IslandKind.INFANT ? 16 : 5;
        for (int i = 0; i < attempts; i++) {
            int x = box.minX() + 2 + random.nextInt(Math.max(1, box.getXSpan() - 4));
            int z = box.minZ() + 2 + random.nextInt(Math.max(1, box.getZSpan() - 4));
            if (!boundingBox.isInside(x, center.getY(), z)) continue;
            if (shapeDistance(x, z) > 0.85 || volcanoDistance(x, z) < 1.05) continue;
            if (kind == IslandKind.INFANT && Math.abs(x - center.getX()) < 9 && Math.abs(z - center.getZ()) < 9) continue;
            int top = topSolid(level, x, z);
            BlockPos ground = new BlockPos(x, top, z);
            if (!level.getBlockState(ground).is(Blocks.GRASS_BLOCK)) continue;
            ResourceKey<ConfiguredFeature<?, ?>> tree;
            if (kind == IslandKind.INFANT) {
                int r = random.nextInt(8);
                tree = r == 0 ? TreeFeatures.MEGA_JUNGLE_TREE : r < 5 ? TreeFeatures.JUNGLE_TREE_NO_VINE : TreeFeatures.JUNGLE_BUSH;
            } else {
                int r = random.nextInt(4);
                tree = r == 0 ? TreeFeatures.OAK : r == 1 ? TreeFeatures.JUNGLE_TREE_NO_VINE : TreeFeatures.JUNGLE_BUSH;
            }
            level.registryAccess().registry(Registries.CONFIGURED_FEATURE)
                    .flatMap(reg -> reg.getHolder(tree))
                    .map(Holder::value)
                    .ifPresent(feature -> feature.place(level, generator, random, ground.above()));
        }
        // Ground cover.
        int cover = kind == IslandKind.INFANT ? 40 : 18;
        for (int i = 0; i < cover; i++) {
            int x = box.minX() + random.nextInt(box.getXSpan());
            int z = box.minZ() + random.nextInt(box.getZSpan());
            if (!boundingBox.isInside(x, center.getY(), z)) continue;
            BlockPos above = new BlockPos(x, topSolid(level, x, z) + 1, z);
            if (level.getBlockState(above.below()).is(Blocks.GRASS_BLOCK) && level.getBlockState(above).isAir()) {
                level.setBlock(above, random.nextInt(3) == 0 ? Blocks.FERN.defaultBlockState() : Blocks.SHORT_GRASS.defaultBlockState(), 2);
            }
        }
    }

    // ------------------------------------------------------------------ Monster Island

    private void placeMonsterLairs(WorldGenLevel level, BoundingBox box) {
        RandomSource r = RandomSource.create(seed);
        // Godzilla rests on the sea floor just off the coast.
        double a = Math.toRadians(200);
        int gx = center.getX() + (int) (Math.cos(a) * kind.radius * 1.2);
        int gz = center.getZ() + (int) (Math.sin(a) * kind.radius * 1.2);
        if (box.isInside(gx, center.getY(), gz)) {
            placeLair(level, new BlockPos(gx, topSolid(level, gx, gz) + 1, gz), ModEntities.GODZILLA.get(), r.nextInt(2));
        }
        // Rodan nests above the lava in the crater.
        int rx = volcanoX(), rz = volcanoZ();
        if (box.isInside(rx, center.getY(), rz)) placeLair(level, new BlockPos(rx, lavaLevel() + 4, rz), ModEntities.RODAN.get(), -1);
        // Anguirus roams the plateau.
        double b = Math.toRadians(40);
        int ax = center.getX() + (int) (Math.cos(b) * kind.radius * 0.45);
        int az = center.getZ() + (int) (Math.sin(b) * kind.radius * 0.45);
        if (box.isInside(ax, center.getY(), az)) {
            placeLair(level, new BlockPos(ax, topSolid(level, ax, az) + 1, az), ModEntities.ANGUIRUS.get(), -1);
        }
    }

    /** Highest block that isn't air, water or foliage (heightmaps can be stale after we reshape the terrain). */
    private int topSolid(WorldGenLevel level, int x, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, boundingBox.maxY(), z);
        while (pos.getY() > boundingBox.minY()) {
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && state.getFluidState().isEmpty() && !state.is(net.minecraft.tags.BlockTags.LEAVES)
                    && !state.is(net.minecraft.tags.BlockTags.LOGS) && !state.is(net.minecraft.tags.BlockTags.REPLACEABLE)) {
                return pos.getY();
            }
            pos.move(0, -1, 0);
        }
        return center.getY() - 20;
    }

    private void placeLair(WorldGenLevel level, BlockPos pos, EntityType<?> type, int variant) {
        level.setBlock(pos, ModBlocks.KAIJU_LAIR.get().defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof KaijuLairBlockEntity lair) {
            lair.setKaiju(BuiltInRegistries.ENTITY_TYPE.getKey(type), variant);
        }
    }

    // ------------------------------------------------------------------ Infant Island

    private void buildShrine(WorldGenLevel level, BoundingBox box) {
        int cx = center.getX(), cz = center.getZ();
        int base = surfaceHeight(cx, cz, center.getY() - 20);
        RandomSource r = RandomSource.create(seed ^ 0x5EED);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                int x = cx + dx, z = cz + dz;
                if (!box.isInside(x, base, z)) continue;
                // Level the ground and lay a weathered stone floor.
                for (int y = base - 3; y < base; y++) level.setBlock(pos.set(x, y, z), Blocks.STONE.defaultBlockState(), 2);
                BlockState floor = switch (r.nextInt(4)) {
                    case 0 -> Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
                    case 1 -> Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
                    default -> Blocks.STONE_BRICKS.defaultBlockState();
                };
                level.setBlock(pos.set(x, base, z), floor, 2);
                for (int y = base + 1; y <= base + 8; y++) level.setBlock(pos.set(x, y, z), Blocks.AIR.defaultBlockState(), 2);
                boolean corner = Math.abs(dx) == 4 && Math.abs(dz) == 4;
                if (corner) {
                    for (int y = base + 1; y <= base + 4; y++) level.setBlock(pos.set(x, y, z), Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState(), 2);
                    level.setBlock(pos.set(x, base + 5, z), Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);
                }
                if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) level.setBlock(pos.set(x, base, z), Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);
            }
        }
        if (box.isInside(cx, base, cz)) {
            level.setBlock(new BlockPos(cx, base + 1, cz), ModBlocks.MOTHRA_EGG.get().defaultBlockState().setValue(MothraEggBlock.HATCH, 0), 2);
        }
    }
}

package com.reiko45.godzilla.world;

import com.reiko45.godzilla.ReikoGodzilla;
import com.reiko45.godzilla.block.KaijuLairBlockEntity;
import com.reiko45.godzilla.entity.monsterverse.KongEntity;
import com.reiko45.godzilla.registry.ModBlocks;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Builds a MonsterVerse site block by block. Everything is placed relative to the centre on the floor; only blocks
 * inside the chunk being generated are written.
 */
public class MonsterverseSitePiece extends StructurePiece {
    private static final ResourceKey<LootTable> THRONE_LOOT = ResourceKey.create(Registries.LOOT_TABLE, ReikoGodzilla.id("chests/kong_throne_room"));
    private static final ResourceKey<LootTable> SKAR_LOOT = ResourceKey.create(Registries.LOOT_TABLE, ReikoGodzilla.id("chests/skar_kingdom"));

    private final SiteKind kind;
    private final BlockPos center;
    private final long seed;

    public MonsterverseSitePiece(SiteKind kind, BlockPos center, long seed) {
        super(ModStructures.MONSTERVERSE_SITE_PIECE.get(), 0, makeBox(kind, center));
        this.kind = kind;
        this.center = center;
        this.seed = seed;
    }

    public MonsterverseSitePiece(CompoundTag tag) {
        super(ModStructures.MONSTERVERSE_SITE_PIECE.get(), tag);
        this.kind = SiteKind.byName(tag.getString("Site"));
        this.center = new BlockPos(tag.getInt("CX"), tag.getInt("CY"), tag.getInt("CZ"));
        this.seed = tag.getLong("Seed");
    }

    private static BoundingBox makeBox(SiteKind kind, BlockPos c) {
        int r = kind.radius + 3;
        int down = kind == SiteKind.VORTEX ? 12 : 6;
        return new BoundingBox(c.getX() - r, c.getY() - down, c.getZ() - r, c.getX() + r, c.getY() + kind.headroom + 4, c.getZ() + r);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("Site", kind.getSerializedName());
        tag.putInt("CX", center.getX());
        tag.putInt("CY", center.getY());
        tag.putInt("CZ", center.getZ());
        tag.putLong("Seed", seed);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator generator, RandomSource random,
                            BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
        RandomSource r = RandomSource.create(seed);
        switch (kind) {
            case THRONE_ROOM -> buildThroneRoom(level, box, r);
            case SKAR_KINGDOM -> buildSkarKingdom(level, box, r);
            case TIAMAT_LAIR -> buildTiamatLair(level, box, r);
            case VORTEX -> buildSurfaceVortex(level, box, r);
            case HOLLOW_VORTEX -> buildHollowVortex(level, box);
            case METHUSELAH -> lair(level, box, center, ModEntities.METHUSELAH.get(), -1);
            case BEHEMOTH -> lair(level, box, center, ModEntities.BEHEMOTH.get(), -1);
        }
    }

    // ------------------------------------------------------------------ helpers

    private void set(WorldGenLevel level, BoundingBox box, int x, int y, int z, BlockState state) {
        if (!box.isInside(x, y, z)) return;
        BlockPos pos = new BlockPos(x, y, z);
        if (level.getBlockState(pos).is(Blocks.BEDROCK)) return;
        level.setBlock(pos, state, 2);
    }

    private void lair(WorldGenLevel level, BoundingBox box, BlockPos pos, EntityType<?> type, int variant) {
        if (!box.isInside(pos)) return;
        level.setBlock(pos, ModBlocks.KAIJU_LAIR.get().defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof KaijuLairBlockEntity lair) lair.setKaiju(BuiltInRegistries.ENTITY_TYPE.getKey(type), variant);
    }

    private void chest(WorldGenLevel level, BoundingBox box, BlockPos pos, Direction facing, ResourceKey<LootTable> loot, RandomSource r) {
        if (!box.isInside(pos)) return;
        level.setBlock(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), 2);
        RandomizableContainer.setBlockEntityLootTable(level, r, pos, loot);
    }

    /** Hollow out a dome so a Titan fits (keeps bedrock). */
    private void carveDome(WorldGenLevel level, BoundingBox box, int radius, int height) {
        int cx = center.getX(), cy = center.getY(), cz = center.getZ();
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                double d = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz)) / radius;
                if (d > 1.0) continue;
                int top = cy + (int) (height * Math.sqrt(1 - d * d));
                for (int y = cy; y <= top; y++) set(level, box, x, y, z, Blocks.AIR.defaultBlockState());
            }
        }
    }

    private void disc(WorldGenLevel level, BoundingBox box, int radius, int y, int depth, java.util.function.Function<RandomSource, BlockState> top, RandomSource r) {
        int cx = center.getX(), cz = center.getZ();
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                if ((x - cx) * (x - cx) + (z - cz) * (z - cz) > radius * radius) continue;
                BlockState t = top.apply(r);
                set(level, box, x, y, z, t);
                for (int d = 1; d <= depth; d++) set(level, box, x, y - d, z, Blocks.STONE.defaultBlockState());
            }
        }
    }

    private static BlockState templeStone(RandomSource r) {
        int i = r.nextInt(10);
        return i < 4 ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState() : i < 6 ? Blocks.CRACKED_STONE_BRICKS.defaultBlockState()
                : i < 7 ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState();
    }

    // ------------------------------------------------------------------ Kong's throne room

    /** Godzilla vs. Kong: the temple of Kong's ancestors, where he found the axe and Apex found its power source. */
    private void buildThroneRoom(WorldGenLevel level, BoundingBox box, RandomSource r) {
        int cx = center.getX(), y0 = center.getY(), cz = center.getZ();
        carveDome(level, box, 17, 24);
        disc(level, box, 15, y0 - 1, 3, MonsterverseSitePiece::templeStone, r);
        // Ring of pillars crowned with light.
        for (int i = 0; i < 10; i++) {
            double a = i * Math.PI * 2 / 10;
            int px = cx + (int) Math.round(Math.cos(a) * 13), pz = cz + (int) Math.round(Math.sin(a) * 13);
            for (int dx = 0; dx <= 1; dx++) for (int dz = 0; dz <= 1; dz++) {
                for (int y = y0; y < y0 + 15; y++) set(level, box, px + dx, y, pz + dz, y % 4 == 0 ? Blocks.CHISELED_STONE_BRICKS.defaultBlockState() : templeStone(r));
                set(level, box, px + dx, y0 + 15, pz + dz, Blocks.SEA_LANTERN.defaultBlockState());
            }
        }
        // The throne: a stepped dais and a towering seat back.
        int tz = cz - 10;
        for (int x = cx - 5; x <= cx + 5; x++) {
            for (int z = tz - 3; z <= tz + 3; z++) {
                int h = z >= tz + 2 ? 1 : z >= tz ? 2 : 3;
                for (int y = y0; y < y0 + h; y++) set(level, box, x, y, z, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
            }
            for (int y = y0 + 3; y < y0 + 16; y++) set(level, box, x, y, tz - 3, Math.abs(x - cx) == 5 || y == y0 + 15 ? Blocks.CHISELED_STONE_BRICKS.defaultBlockState() : Blocks.DEEPSLATE_BRICKS.defaultBlockState());
        }
        for (int z = tz - 3; z <= tz + 1; z++) {
            for (int y = y0 + 3; y < y0 + 7; y++) {
                set(level, box, cx - 5, y, z, Blocks.DEEPSLATE_BRICKS.defaultBlockState());
                set(level, box, cx + 5, y, z, Blocks.DEEPSLATE_BRICKS.defaultBlockState());
            }
        }
        set(level, box, cx, y0 + 12, tz - 2, Blocks.SHROOMLIGHT.defaultBlockState());
        // The axe's pedestal, with its chest.
        for (int x = cx - 1; x <= cx + 1; x++) for (int z = cz - 5; z <= cz - 3; z++) set(level, box, x, y0, z, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        set(level, box, cx, y0 + 1, cz - 4, Blocks.AMETHYST_BLOCK.defaultBlockState());
        chest(level, box, new BlockPos(cx, y0 + 2, cz - 4), Direction.SOUTH, THRONE_LOOT, r);
        // Glyphs glowing in the floor.
        for (int i = 0; i < 24; i++) {
            int x = cx + r.nextInt(25) - 12, z = cz + r.nextInt(25) - 12;
            if ((x - cx) * (x - cx) + (z - cz) * (z - cz) < 140) set(level, box, x, y0 - 1, z, r.nextBoolean() ? Blocks.GLOWSTONE.defaultBlockState() : Blocks.SEA_LANTERN.defaultBlockState());
        }
        lair(level, box, new BlockPos(cx, y0, cz + 4), ModEntities.KONG.get(), KongEntity.ADULT);
    }

    // ------------------------------------------------------------------ the Skar King's realm

    /** Godzilla x Kong: The New Empire - a lava-lit cavern where the Skar King keeps Shimo in chains. */
    private void buildSkarKingdom(WorldGenLevel level, BoundingBox box, RandomSource r) {
        int cx = center.getX(), y0 = center.getY(), cz = center.getZ();
        carveDome(level, box, 24, 28);
        disc(level, box, 23, y0 - 1, 3, rr -> {
            int i = rr.nextInt(12);
            return i < 5 ? Blocks.BLACKSTONE.defaultBlockState() : i < 8 ? Blocks.BASALT.defaultBlockState() : i < 9 ? Blocks.MAGMA_BLOCK.defaultBlockState()
                    : Blocks.POLISHED_BLACKSTONE.defaultBlockState();
        }, r);
        // A moat of lava around the edge.
        for (int x = cx - 23; x <= cx + 23; x++) {
            for (int z = cz - 23; z <= cz + 23; z++) {
                double d = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
                if (d > 19.5 && d < 22) set(level, box, x, y0 - 1, z, Blocks.LAVA.defaultBlockState());
                else if (d >= 22 && d < 23.5) set(level, box, x, y0, z, Blocks.BLACKSTONE.defaultBlockState());
            }
        }
        // Lava falls down the far wall.
        for (int i = 0; i < 4; i++) {
            double a = Math.PI * 0.75 + i * 0.25;
            BlockPos fall = new BlockPos(cx + (int) (Math.cos(a) * 21), y0 + 22, cz + (int) (Math.sin(a) * 21));
            if (box.isInside(fall)) {
                level.setBlock(fall, Blocks.LAVA.defaultBlockState(), 2);
                level.scheduleTick(fall, net.minecraft.world.level.material.Fluids.LAVA, 1);
            }
        }
        // Chains hanging over Shimo's place.
        int sx = cx + 11;
        for (int[] c : new int[][]{{sx - 5, cz - 5}, {sx + 5, cz - 5}, {sx - 5, cz + 5}, {sx + 5, cz + 5}}) {
            for (int y = y0; y < y0 + 26; y++) set(level, box, c[0], y, c[1], Blocks.CHAIN.defaultBlockState().setValue(ChainBlock.AXIS, Direction.Axis.Y));
        }
        // A heap of bones for a throne.
        int bx = cx - 12;
        for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) {
            int h = 4 - Math.max(Math.abs(dx), Math.abs(dz));
            for (int y = y0; y < y0 + h; y++) set(level, box, bx + dx, y, cz + dz, r.nextInt(5) == 0 ? Blocks.SOUL_SAND.defaultBlockState() : Blocks.BONE_BLOCK.defaultBlockState());
        }
        chest(level, box, new BlockPos(bx, y0 + 4, cz), Direction.EAST, SKAR_LOOT, r);
        lair(level, box, new BlockPos(sx, y0, cz), ModEntities.SHIMO.get(), -1);
        lair(level, box, new BlockPos(bx + 5, y0, cz), ModEntities.SKAR_KING.get(), -1);
        lair(level, box, new BlockPos(cx - 4, y0, cz - 8), ModEntities.GREAT_APE.get(), -1);
        lair(level, box, new BlockPos(cx - 4, y0, cz + 8), ModEntities.GREAT_APE.get(), -1);
    }

    // ------------------------------------------------------------------ Tiamat's lair

    /** Godzilla x Kong: The New Empire - an ice cavern beneath the Arctic holding the planet's largest store of solar radiation. */
    private void buildTiamatLair(WorldGenLevel level, BoundingBox box, RandomSource r) {
        int cx = center.getX(), y0 = center.getY(), cz = center.getZ();
        int radius = 12;
        for (int x = cx - radius - 1; x <= cx + radius + 1; x++) {
            for (int z = cz - radius - 1; z <= cz + radius + 1; z++) {
                for (int y = y0 - 1; y <= y0 + radius + 1; y++) {
                    double d = Math.sqrt((x - cx) * (x - cx) + (y - y0) * (y - y0) * 1.4 + (z - cz) * (z - cz));
                    boolean entrance = z < cz - radius + 4 && Math.abs(x - cx) <= 2 && y <= y0 + 4;
                    if (y == y0 - 1 && d <= radius) {
                        set(level, box, x, y, z, r.nextInt(4) == 0 ? Blocks.PRISMARINE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState());
                    } else if (d <= radius - 1 || entrance && d <= radius + 1) {
                        if (y >= y0) set(level, box, x, y, z, Blocks.WATER.defaultBlockState());
                    } else if (d <= radius + 0.5) {
                        set(level, box, x, y, z, r.nextInt(3) == 0 ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState());
                    }
                }
            }
        }
        // The stored solar radiation, glowing pink.
        for (int x = cx - 1; x <= cx + 1; x++) for (int z = cz - 1; z <= cz + 1; z++) set(level, box, x, y0, z, Blocks.PACKED_ICE.defaultBlockState());
        for (int[] c : new int[][]{{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}}) set(level, box, cx + c[0], y0 + 1, cz + c[1], ModBlocks.SOLAR_CRYSTAL.get().defaultBlockState());
        set(level, box, cx, y0 + 2, cz, ModBlocks.SOLAR_CRYSTAL.get().defaultBlockState());
        for (int i = 0; i < 8; i++) {
            int x = cx + r.nextInt(17) - 8, z = cz + r.nextInt(17) - 8;
            set(level, box, x, y0, z, Blocks.SEA_LANTERN.defaultBlockState());
        }
        lair(level, box, new BlockPos(cx + 6, y0, cz + 4), ModEntities.TIAMAT.get(), -1);
    }

    // ------------------------------------------------------------------ vortices

    /** Godzilla vs. Kong: the gravity vortex in the Antarctic ice, ringed by Monarch's markers. */
    private void buildSurfaceVortex(WorldGenLevel level, BoundingBox box, RandomSource r) {
        int cx = center.getX(), y0 = center.getY(), cz = center.getZ();
        for (int x = cx - 9; x <= cx + 9; x++) {
            for (int z = cz - 9; z <= cz + 9; z++) {
                double d = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
                if (d <= 4.5) {
                    // The pit.
                    for (int y = y0 - 8; y <= y0 + 6; y++) {
                        BlockState s = y == y0 - 8 ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
                                : y == y0 - 7 && d <= 2.5 ? ModBlocks.HOLLOW_EARTH_VORTEX.get().defaultBlockState()
                                : Blocks.AIR.defaultBlockState();
                        set(level, box, x, y, z, s);
                    }
                } else if (d <= 5.6) {
                    for (int y = y0 - 8; y < y0; y++) set(level, box, x, y, z, r.nextInt(3) == 0 ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState());
                    set(level, box, x, y0, z, Blocks.PACKED_ICE.defaultBlockState());
                } else if (d <= 9) {
                    set(level, box, x, y0 - 1, z, Blocks.SNOW_BLOCK.defaultBlockState());
                    for (int y = y0; y <= y0 + 5; y++) set(level, box, x, y, z, Blocks.AIR.defaultBlockState());
                    if (d > 8 && r.nextInt(5) == 0) {
                        int h = 2 + r.nextInt(4);
                        for (int y = y0; y < y0 + h; y++) set(level, box, x, y, z, Blocks.PACKED_ICE.defaultBlockState());
                    }
                }
            }
        }
        // Warning beacons at the rim.
        for (int i = 0; i < 4; i++) {
            double a = i * Math.PI / 2 + Math.PI / 4;
            int x = cx + (int) Math.round(Math.cos(a) * 7), z = cz + (int) Math.round(Math.sin(a) * 7);
            set(level, box, x, y0, z, Blocks.IRON_BLOCK.defaultBlockState());
            set(level, box, x, y0 + 1, z, Blocks.IRON_BARS.defaultBlockState());
            set(level, box, x, y0 + 2, z, Blocks.REDSTONE_LAMP.defaultBlockState().setValue(net.minecraft.world.level.block.RedstoneLampBlock.LIT, true));
            set(level, box, x, y0 - 1, z, Blocks.REDSTONE_BLOCK.defaultBlockState());
        }
    }

    /** A vortex back to the surface on a Hollow Earth cavern floor. */
    private void buildHollowVortex(WorldGenLevel level, BoundingBox box) {
        int cx = center.getX(), y0 = center.getY(), cz = center.getZ();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                boolean corner = Math.abs(dx) == 3 && Math.abs(dz) == 3;
                boolean rim = Math.abs(dx) == 3 || Math.abs(dz) == 3;
                set(level, box, cx + dx, y0 - 2, cz + dz, Blocks.CRYING_OBSIDIAN.defaultBlockState());
                set(level, box, cx + dx, y0 - 1, cz + dz, rim ? Blocks.CALCITE.defaultBlockState()
                        : Math.abs(dx) <= 1 && Math.abs(dz) <= 1 ? ModBlocks.HOLLOW_EARTH_VORTEX.get().defaultBlockState() : Blocks.AMETHYST_BLOCK.defaultBlockState());
                for (int y = y0; y < y0 + 5; y++) set(level, box, cx + dx, y, cz + dz, corner && y < y0 + 3 ? Blocks.AMETHYST_BLOCK.defaultBlockState() : Blocks.AIR.defaultBlockState());
                if (corner) set(level, box, cx + dx, y0 + 3, cz + dz, Blocks.SEA_LANTERN.defaultBlockState());
            }
        }
    }
}

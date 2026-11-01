package com.reiko45.godzilla.world;

import com.reiko45.godzilla.ReikoGodzilla;
import com.reiko45.godzilla.block.AlienTransmitterBlock;
import com.reiko45.godzilla.block.GuardianShrineBlock;
import com.reiko45.godzilla.block.KaijuLairBlockEntity;
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
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Builds a Showa/Millennium site block by block around its centre. Everything is placed relative to the centre on the
 * ground; only blocks inside the chunk being generated are written.
 */
public class ClassicSitePiece extends StructurePiece {
    private final ClassicSiteKind kind;
    private final BlockPos center;
    /** Surface height above the site (Seatopia's shaft climbs to it). */
    private final int surface;
    private final long seed;

    public ClassicSitePiece(ClassicSiteKind kind, BlockPos center, int surface, long seed) {
        super(ModStructures.CLASSIC_SITE_PIECE.get(), 0, makeBox(kind, center, surface));
        this.kind = kind;
        this.center = center;
        this.surface = surface;
        this.seed = seed;
    }

    public ClassicSitePiece(CompoundTag tag) {
        super(ModStructures.CLASSIC_SITE_PIECE.get(), tag);
        this.kind = ClassicSiteKind.byName(tag.getString("Site"));
        this.center = new BlockPos(tag.getInt("CX"), tag.getInt("CY"), tag.getInt("CZ"));
        this.surface = tag.getInt("Surface");
        this.seed = tag.getLong("Seed");
    }

    private static BoundingBox makeBox(ClassicSiteKind kind, BlockPos c, int surface) {
        int r = kind.radius + 3;
        int top = Math.max(c.getY() + kind.headroom + 4, surface + 4);
        return new BoundingBox(c.getX() - r, c.getY() - 10, c.getZ() - r, c.getX() + r, top, c.getZ() + r);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("Site", kind.getSerializedName());
        tag.putInt("CX", center.getX());
        tag.putInt("CY", center.getY());
        tag.putInt("CZ", center.getZ());
        tag.putInt("Surface", surface);
        tag.putLong("Seed", seed);
    }

    private static ResourceKey<LootTable> loot(String name) {
        return ResourceKey.create(Registries.LOOT_TABLE, ReikoGodzilla.id("chests/" + name));
    }

    /** Block placement relative to the site centre, clipped to the chunk being generated. Thread-safe: one per call. */
    private record B(WorldGenLevel level, BoundingBox box, int cx, int cy, int cz) {
        void set(int dx, int dy, int dz, BlockState state) {
            int x = cx + dx, y = cy + dy, z = cz + dz;
            if (!box.isInside(x, y, z)) return;
            BlockPos pos = new BlockPos(x, y, z);
            if (level.getBlockState(pos).is(Blocks.BEDROCK)) return;
            level.setBlock(pos, state, 2);
        }

        BlockState get(int dx, int dy, int dz) {
            return level.getBlockState(new BlockPos(cx + dx, cy + dy, cz + dz));
        }

        void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
            for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++)
                for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++)
                    for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) set(x, y, z, state);
        }

        /** Walls, floor and roof of {@code wall}; air inside. */
        void hollow(int x0, int y0, int z0, int x1, int y1, int z1, BlockState wall) {
            for (int x = x0; x <= x1; x++)
                for (int y = y0; y <= y1; y++)
                    for (int z = z0; z <= z1; z++) {
                        boolean edge = x == x0 || x == x1 || y == y0 || y == y1 || z == z0 || z == z1;
                        set(x, y, z, edge ? wall : Blocks.AIR.defaultBlockState());
                    }
        }

        /** Solid ground under a pad: fills air and water below the floor down to the terrain. */
        void foundation(int radius, BlockState state) {
            for (int dx = -radius; dx <= radius; dx++)
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz > radius * radius) continue;
                    for (int dy = -1; dy >= -9; dy--) {
                        BlockState s = get(dx, dy, dz);
                        if (!s.isAir() && s.getFluidState().isEmpty() && !s.canBeReplaced()) break;
                        set(dx, dy, dz, state);
                    }
                }
        }

        void clearAbove(int radius, int height) {
            for (int dx = -radius; dx <= radius; dx++)
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz > radius * radius) continue;
                    for (int dy = 1; dy <= height; dy++) set(dx, dy, dz, Blocks.AIR.defaultBlockState());
                }
        }

        void disc(int radius, int dy, java.util.function.Function<Integer, BlockState> byRing) {
            for (int dx = -radius; dx <= radius; dx++)
                for (int dz = -radius; dz <= radius; dz++) {
                    double d = Math.sqrt(dx * dx + dz * dz);
                    if (d <= radius) set(dx, dy, dz, byRing.apply((int) d));
                }
        }
    }

    private void lair(B b, int dx, int dy, int dz, EntityType<?> type, int variant) {
        int x = b.cx + dx, y = b.cy + dy, z = b.cz + dz;
        if (!b.box.isInside(x, y, z)) return;
        BlockPos pos = new BlockPos(x, y, z);
        b.level.setBlock(pos, ModBlocks.KAIJU_LAIR.get().defaultBlockState(), 2);
        if (b.level.getBlockEntity(pos) instanceof KaijuLairBlockEntity lair) lair.setKaiju(BuiltInRegistries.ENTITY_TYPE.getKey(type), variant);
    }

    private void chest(B b, int dx, int dy, int dz, Direction facing, String table, RandomSource r) {
        int x = b.cx + dx, y = b.cy + dy, z = b.cz + dz;
        if (!b.box.isInside(x, y, z)) return;
        BlockPos pos = new BlockPos(x, y, z);
        b.level.setBlock(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), 2);
        RandomizableContainer.setBlockEntityLootTable(b.level, r, pos, loot(table));
    }

    private static BlockState transmitter(AlienSignal signal) {
        return ModBlocks.ALIEN_TRANSMITTER.get().defaultBlockState().setValue(AlienTransmitterBlock.SIGNAL, signal);
    }

    private static BlockState shrine(GuardianShrineBlock.Guardian guardian) {
        return ModBlocks.GUARDIAN_SHRINE.get().defaultBlockState().setValue(GuardianShrineBlock.GUARDIAN, guardian);
    }

    private static BlockState ladder(Direction facing) {
        return Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, facing);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator generator, RandomSource random,
                            BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
        RandomSource r = RandomSource.create(seed);
        B b = new B(level, box, center.getX(), center.getY(), center.getZ());
        switch (kind) {
            case GODZILLA_TOWER -> godzillaTower(b, r);
            case SEATOPIA -> seatopia(b, r);
            case AZUMI_SHRINE -> azumiShrine(b, r);
            case BARAGON_SHRINE -> baragonShrine(b, r);
            case MOTHRA_SHRINE -> mothraShrine(b, r);
            case GHIDORAH_SHRINE -> ghidorahShrine(b, r);
            case UFO_WRECK -> ufoWreck(b, r);
            case MEGANULON_NEST -> meganulonNest(b, r);
            case KIRYU_HANGAR -> kiryuHangar(b, r);
            case XILIEN_MOTHERSHIP -> xilienMothership(b, r);
        }
    }

    // ------------------------------------------------------------------ Godzilla Tower (Godzilla vs. Gigan)

    private void godzillaTower(B b, RandomSource r) {
        BlockState[] rings = {Blocks.YELLOW_CONCRETE.defaultBlockState(), Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState(), Blocks.PINK_CONCRETE.defaultBlockState(),
                Blocks.SMOOTH_STONE.defaultBlockState()};
        b.foundation(14, Blocks.STONE.defaultBlockState());
        b.clearAbove(14, 30);
        b.disc(14, 0, ring -> rings[ring % rings.length]);
        BlockState skin = Blocks.GREEN_TERRACOTTA.defaultBlockState();
        // Body
        b.hollow(-3, 1, -3, 3, 19, 3, skin);
        b.fill(0, 1, 3, 0, 2, 3, Blocks.AIR.defaultBlockState());
        for (int y = 1; y <= 19; y++) b.set(0, y, -2, ladder(Direction.SOUTH));
        // Head with the control room
        b.hollow(-3, 19, -1, 3, 25, 5, skin);
        b.fill(-2, 19, 0, 2, 19, 4, Blocks.SMOOTH_STONE.defaultBlockState());
        b.set(0, 19, -2, ladder(Direction.SOUTH));
        b.set(0, 19, -1, Blocks.AIR.defaultBlockState());
        b.set(-2, 23, 5, Blocks.YELLOW_CONCRETE.defaultBlockState());
        b.set(2, 23, 5, Blocks.YELLOW_CONCRETE.defaultBlockState());
        b.set(0, 21, 5, ModBlocks.TOWER_LASER.get().defaultBlockState());
        b.set(0, 20, 2, transmitter(AlienSignal.NEBULA_M));
        b.set(0, 24, 2, Blocks.SEA_LANTERN.defaultBlockState());
        chest(b, 2, 20, 1, Direction.WEST, "godzilla_tower", r);
        // Dorsal plates down the back and stubby arms
        for (int y = 6; y <= 22; y += 3) {
            b.set(0, y, -4, Blocks.WHITE_CONCRETE.defaultBlockState());
            b.set(0, y + 1, -4, Blocks.WHITE_CONCRETE.defaultBlockState());
            b.set(0, y + 1, -5, Blocks.WHITE_CONCRETE.defaultBlockState());
        }
        b.fill(-5, 13, 0, -4, 14, 2, skin);
        b.fill(4, 13, 0, 5, 14, 2, skin);
        // Tail curling along the plaza
        for (int i = 0; i < 9; i++) {
            int w = Math.max(0, 1 - i / 4);
            b.fill(-w, 1, -4 - i, w, i < 5 ? 2 : 1, -4 - i, skin);
        }
        // Ticket booths
        for (int s : new int[]{-1, 1}) {
            b.hollow(s * 10 - 1, 1, 9, s * 10 + 1, 3, 11, Blocks.RED_WOOL.defaultBlockState());
            b.fill(s * 10 - 1, 4, 9, s * 10 + 1, 4, 11, Blocks.WHITE_WOOL.defaultBlockState());
            b.set(s * 10, 2, 11, Blocks.AIR.defaultBlockState());
        }
        lair(b, 8, 1, 6, ModEntities.GIGAN.get(), 0);
    }

    // ------------------------------------------------------------------ Seatopia (Godzilla vs. Megalon)

    private void seatopia(B b, RandomSource r) {
        int radius = 22, height = 16;
        // The cavern
        for (int dx = -radius; dx <= radius; dx++)
            for (int dz = -radius; dz <= radius; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz) / radius;
                if (d > 1.0) continue;
                int top = (int) (height * Math.sqrt(1 - d * d));
                for (int dy = 1; dy <= top; dy++) b.set(dx, dy, dz, Blocks.AIR.defaultBlockState());
                b.set(dx, 0, dz, (dx + dz) % 3 == 0 ? Blocks.DARK_PRISMARINE.defaultBlockState() : Blocks.PRISMARINE_BRICKS.defaultBlockState());
            }
        // The artificial sun
        for (int dx = -3; dx <= 3; dx++)
            for (int dy = -3; dy <= 3; dy++)
                for (int dz = -3; dz <= 3; dz++) {
                    int d2 = dx * dx + dy * dy + dz * dz;
                    if (d2 <= 9) b.set(dx, 12 + dy, dz, d2 <= 2 ? Blocks.GLOWSTONE.defaultBlockState() : Blocks.SHROOMLIGHT.defaultBlockState());
                }
        // Ring of columns
        for (int i = 0; i < 10; i++) {
            double a = Math.PI * 2 * i / 10;
            int x = (int) Math.round(Math.cos(a) * 14), z = (int) Math.round(Math.sin(a) * 14);
            b.fill(x, 1, z, x, 8, z, Blocks.QUARTZ_PILLAR.defaultBlockState());
            b.set(x, 9, z, Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState());
        }
        // Temple and the guidance beacon
        b.fill(-3, 1, -3, 3, 1, 3, Blocks.QUARTZ_BRICKS.defaultBlockState());
        b.set(0, 2, 0, Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState());
        b.set(0, 3, 0, transmitter(AlienSignal.SEATOPIA));
        chest(b, 2, 2, 2, Direction.NORTH, "seatopia", r);
        // Statues of Seatopia's gods
        for (int i = 0; i < 4; i++) {
            double a = Math.PI / 4 + Math.PI / 2 * i;
            int x = (int) Math.round(Math.cos(a) * 18), z = (int) Math.round(Math.sin(a) * 18);
            b.fill(x - 1, 1, z - 1, x + 1, 4, z, Blocks.ANDESITE.defaultBlockState());
            b.fill(x - 1, 5, z - 1, x + 1, 7, z + 1, Blocks.POLISHED_ANDESITE.defaultBlockState());
            b.set(x, 6, z + (z > 0 ? -1 : 1), Blocks.SEA_LANTERN.defaultBlockState());
        }
        // Megalon's glowing spire
        for (int dy = 1; dy <= 11; dy++) {
            int w = dy < 4 ? 1 : 0;
            b.fill(-12 - w, dy, -w, -12 + w, dy, w, dy % 3 == 0 ? Blocks.AMETHYST_BLOCK.defaultBlockState() : Blocks.CRYING_OBSIDIAN.defaultBlockState());
        }
        lair(b, -12, 1, 5, ModEntities.MEGALON.get(), -1);
        // The way down from the surface
        int sx = 19;
        int climb = surface - b.cy + 1;
        for (int dy = 1; dy <= climb; dy++) {
            for (int ox = -2; ox <= 2; ox++)
                for (int oz = -2; oz <= 2; oz++) {
                    boolean wall = Math.abs(ox) == 2 || Math.abs(oz) == 2;
                    if (dy > 14 || wall) b.set(sx + ox, dy, oz, wall ? Blocks.PRISMARINE_BRICKS.defaultBlockState() : Blocks.AIR.defaultBlockState());
                }
            b.set(sx - 1, dy, 0, ladder(Direction.EAST));
        }
        for (int ox = -2; ox <= 2; ox += 4) b.set(sx + ox, climb, 0, Blocks.SEA_LANTERN.defaultBlockState());
    }

    // ------------------------------------------------------------------ the Azumi shrine (Godzilla vs. Mechagodzilla)

    private void azumiShrine(B b, RandomSource r) {
        b.foundation(16, Blocks.SANDSTONE.defaultBlockState());
        // The cliff where King Caesar sleeps
        for (int dx = -15; dx <= 15; dx++)
            for (int dz = -18; dz <= -1; dz++) {
                double e = 1 - Math.pow(dx / 15.0, 2) - Math.pow((dz + 10) / 9.0, 2);
                if (e <= 0) continue;
                int h = (int) (18 * Math.sqrt(e));
                for (int dy = 0; dy <= h; dy++) b.set(dx, dy, dz, r.nextInt(6) == 0 ? Blocks.SMOOTH_SANDSTONE.defaultBlockState() : Blocks.CALCITE.defaultBlockState());
            }
        // His chamber, open to the shrine
        for (int dx = -6; dx <= 6; dx++)
            for (int dz = -15; dz <= -5; dz++)
                for (int dy = 1; dy <= 13; dy++) {
                    double e = Math.pow(dx / 6.0, 2) + Math.pow((dz + 10) / 5.5, 2) + Math.pow((dy - 4) / 9.0, 2);
                    if (e <= 1) b.set(dx, dy, dz, Blocks.AIR.defaultBlockState());
                }
        b.fill(-2, 1, -5, 2, 8, -1, Blocks.AIR.defaultBlockState());
        lair(b, 0, 1, -10, ModEntities.KING_CAESAR.get(), -1);
        // The shrine
        b.fill(-4, 0, 3, 4, 0, 9, Blocks.STONE_BRICKS.defaultBlockState());
        b.clearAbove(10, 1);
        b.set(0, 1, 5, Blocks.POLISHED_GRANITE.defaultBlockState());
        b.set(2, 1, 5, Blocks.JUKEBOX.defaultBlockState());
        chest(b, -2, 1, 5, Direction.SOUTH, "azumi_shrine", r);
        for (int s : new int[]{-3, 3}) b.fill(s, 1, 10, s, 5, 10, Blocks.RED_CONCRETE.defaultBlockState());
        b.fill(-4, 6, 10, 4, 6, 10, Blocks.RED_CONCRETE.defaultBlockState());
        b.fill(-3, 5, 10, 3, 5, 10, Blocks.RED_CONCRETE.defaultBlockState());
        b.set(-4, 1, 8, Blocks.LANTERN.defaultBlockState());
        b.set(4, 1, 8, Blocks.LANTERN.defaultBlockState());
    }

    // ------------------------------------------------------------------ GMK shrines

    private void torii(B b, int z, int halfWidth, int height) {
        for (int s : new int[]{-halfWidth, halfWidth}) b.fill(s, 1, z, s, height, z, Blocks.RED_CONCRETE.defaultBlockState());
        b.fill(-halfWidth - 1, height + 1, z, halfWidth + 1, height + 1, z, Blocks.BLACK_CONCRETE.defaultBlockState());
        b.fill(-halfWidth, height - 1, z, halfWidth, height - 1, z, Blocks.RED_CONCRETE.defaultBlockState());
    }

    private void baragonShrine(B b, RandomSource r) {
        b.foundation(10, Blocks.STONE.defaultBlockState());
        // A rocky hill with a tunnel in it
        for (int dx = -10; dx <= 10; dx++)
            for (int dz = -10; dz <= 6; dz++) {
                double e = 1 - (dx * dx + (dz + 2) * (dz + 2)) / 81.0;
                if (e <= 0) continue;
                int h = (int) (12 * e);
                for (int dy = 0; dy <= h; dy++) b.set(dx, dy, dz, r.nextInt(5) == 0 ? Blocks.ANDESITE.defaultBlockState() : Blocks.STONE.defaultBlockState());
            }
        b.fill(-1, 1, -6, 1, 4, 8, Blocks.AIR.defaultBlockState());
        b.fill(-3, 1, -8, 3, 5, -5, Blocks.AIR.defaultBlockState());
        for (int z = -4; z <= 6; z += 4) {
            b.fill(-2, 1, z, -2, 4, z, Blocks.SPRUCE_LOG.defaultBlockState());
            b.fill(2, 1, z, 2, 4, z, Blocks.SPRUCE_LOG.defaultBlockState());
            b.fill(-2, 5, z, 2, 5, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
        }
        b.set(0, 1, -7, shrine(GuardianShrineBlock.Guardian.BARAGON));
        b.set(-2, 1, -7, Blocks.CANDLE.defaultBlockState());
        b.set(2, 1, -7, Blocks.CANDLE.defaultBlockState());
        chest(b, 2, 1, -6, Direction.WEST, "gmk_shrine", r);
        torii(b, 10, 2, 5);
    }

    private void mothraShrine(B b, RandomSource r) {
        b.foundation(11, Blocks.DIRT.defaultBlockState());
        b.clearAbove(11, 8);
        // The lake
        for (int dx = -9; dx <= 9; dx++)
            for (int dz = -11; dz <= -1; dz++) {
                double e = (dx * dx) / 81.0 + Math.pow(dz + 6, 2) / 25.0;
                if (e > 1) continue;
                int depth = e < 0.4 ? 3 : e < 0.8 ? 2 : 1;
                for (int dy = 0; dy > -depth; dy--) b.set(dx, dy, dz, Blocks.WATER.defaultBlockState());
                b.set(dx, -depth, dz, Blocks.CLAY.defaultBlockState());
            }
        // The shrine house
        b.fill(-3, 0, 1, 3, 0, 6, Blocks.STONE_BRICKS.defaultBlockState());
        b.hollow(-2, 1, 2, 2, 4, 6, Blocks.SPRUCE_PLANKS.defaultBlockState());
        b.fill(-1, 1, 2, 1, 3, 2, Blocks.AIR.defaultBlockState());
        b.fill(-3, 5, 1, 3, 5, 7, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        b.set(0, 1, 5, shrine(GuardianShrineBlock.Guardian.MOTHRA));
        b.set(-1, 1, 5, Blocks.CANDLE.defaultBlockState());
        chest(b, 1, 1, 5, Direction.NORTH, "gmk_shrine", r);
        torii(b, 9, 2, 5);
    }

    private void ghidorahShrine(B b, RandomSource r) {
        b.foundation(10, Blocks.STONE.defaultBlockState());
        // An icy mound in the dark forest
        for (int dx = -9; dx <= 9; dx++)
            for (int dz = -9; dz <= 9; dz++) {
                double e = 1 - (dx * dx + dz * dz) / 81.0;
                if (e <= 0) continue;
                int h = (int) (9 * e);
                for (int dy = 0; dy <= h; dy++) b.set(dx, dy, dz, dy == h ? Blocks.SNOW_BLOCK.defaultBlockState() : Blocks.STONE.defaultBlockState());
            }
        // The frozen chamber
        for (int dx = -5; dx <= 5; dx++)
            for (int dz = -6; dz <= 4; dz++)
                for (int dy = 0; dy <= 6; dy++) {
                    double e = Math.pow(dx / 5.0, 2) + Math.pow((dz + 1) / 5.0, 2) + Math.pow(dy / 6.0, 2);
                    if (e > 1) continue;
                    b.set(dx, dy, dz, e > 0.72 || dy == 0 ? (r.nextInt(3) == 0 ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState())
                            : Blocks.AIR.defaultBlockState());
                }
        b.fill(-1, 1, 4, 1, 3, 10, Blocks.AIR.defaultBlockState());
        b.set(0, 1, -4, shrine(GuardianShrineBlock.Guardian.GHIDORAH));
        chest(b, 2, 1, -3, Direction.WEST, "gmk_shrine", r);
        b.set(-2, 1, -3, Blocks.SOUL_LANTERN.defaultBlockState());
        torii(b, 10, 2, 5);
    }

    // ------------------------------------------------------------------ the Millennian UFO (Godzilla 2000)

    private void ufoWreck(B b, RandomSource r) {
        for (int dx = -9; dx <= 9; dx++)
            for (int dz = -9; dz <= 9; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > 9) continue;
                b.set(dx, 0, dz, Blocks.GRAY_CONCRETE.defaultBlockState());
                if (d > 7.5) b.set(dx, 1, dz, (int) (Math.atan2(dz, dx) * 4) % 2 == 0 ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState());
                else if (d > 3.5) b.set(dx, 1, dz, Blocks.GRAY_CONCRETE.defaultBlockState());
            }
        for (int dx = -3; dx <= 3; dx++)
            for (int dz = -3; dz <= 3; dz++)
                for (int dy = 1; dy <= 4; dy++) {
                    double e = (dx * dx + dz * dz) / 9.0 + Math.pow((dy - 1) / 3.0, 2);
                    if (e > 1) continue;
                    b.set(dx, dy, dz, e > 0.6 ? Blocks.TINTED_GLASS.defaultBlockState() : Blocks.WATER.defaultBlockState());
                }
        chest(b, 0, 1, 0, Direction.NORTH, "ufo_wreck", r);
        lair(b, 0, 7, 0, ModEntities.MILLENNIAN_UFO.get(), -1);
    }

    // ------------------------------------------------------------------ the Meganulon nest (Godzilla vs. Megaguirus)

    private void meganulonNest(B b, RandomSource r) {
        b.foundation(12, Blocks.STONE.defaultBlockState());
        b.clearAbove(12, 10);
        b.disc(12, 0, ring -> r.nextInt(4) == 0 ? Blocks.MUD.defaultBlockState() : Blocks.GRAY_CONCRETE_POWDER.defaultBlockState());
        // Flooded streets
        for (int i = 0; i < 6; i++) {
            int px = r.nextInt(17) - 8, pz = r.nextInt(17) - 8;
            for (int dx = -2; dx <= 2; dx++)
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx * dx + dz * dz > 5) continue;
                    b.set(px + dx, 0, pz + dz, Blocks.WATER.defaultBlockState());
                    b.set(px + dx, -1, pz + dz, Blocks.WATER.defaultBlockState());
                    b.set(px + dx, -2, pz + dz, Blocks.MUD.defaultBlockState());
                }
            b.set(px + 3, 1, pz, ModBlocks.MEGANULON_EGG.get().defaultBlockState());
            b.set(px + 3, 0, pz, Blocks.GRAY_CONCRETE.defaultBlockState());
        }
        // Ruined buildings
        int[][] blocks = {{-9, -9, 5, 9}, {4, -10, 6, 7}, {-8, 4, 6, 6}};
        for (int[] bl : blocks) {
            int x0 = bl[0], z0 = bl[1], w = bl[2], h = bl[3];
            for (int x = x0; x < x0 + w; x++)
                for (int z = z0; z < z0 + w; z++) {
                    boolean wall = x == x0 || x == x0 + w - 1 || z == z0 || z == z0 + w - 1;
                    if (!wall) continue;
                    int top = h - r.nextInt(4);
                    for (int y = 1; y <= top; y++) {
                        if (y % 3 == 2 && r.nextBoolean()) continue;
                        b.set(x, y, z, r.nextInt(5) == 0 ? Blocks.CRACKED_STONE_BRICKS.defaultBlockState() : Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState());
                    }
                }
        }
        chest(b, -7, 1, -7, Direction.SOUTH, "meganulon_nest", r);
    }

    // ------------------------------------------------------------------ Kiryu's hangar (Godzilla Against Mechagodzilla)

    private void kiryuHangar(B b, RandomSource r) {
        b.foundation(14, Blocks.STONE.defaultBlockState());
        b.fill(-10, 0, -13, 10, 0, 13, Blocks.SMOOTH_STONE.defaultBlockState());
        b.hollow(-10, 0, -13, 10, 17, 13, Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState());
        b.fill(-9, 0, -12, 9, 0, 12, Blocks.SMOOTH_STONE.defaultBlockState());
        for (int z = -12; z <= 12; z += 6) {
            b.fill(-10, 1, z, -10, 16, z, Blocks.IRON_BLOCK.defaultBlockState());
            b.fill(10, 1, z, 10, 16, z, Blocks.IRON_BLOCK.defaultBlockState());
        }
        b.fill(-7, 1, 13, 7, 14, 13, Blocks.AIR.defaultBlockState());
        b.fill(-8, 15, 13, 8, 15, 13, Blocks.YELLOW_CONCRETE.defaultBlockState());
        // Maintenance gantries
        for (int s : new int[]{-7, 7}) {
            for (int z = -8; z <= 8; z += 4) b.fill(s, 1, z, s, 14, z, Blocks.IRON_BARS.defaultBlockState());
            b.fill(s, 14, -8, s, 14, 8, Blocks.IRON_BLOCK.defaultBlockState());
            b.set(s, 1, 0, ModBlocks.KIRYU_CHARGER.get().defaultBlockState());
        }
        b.set(0, 1, -11, ModBlocks.KIRYU_CHARGER.get().defaultBlockState());
        for (int z = -10; z <= 10; z += 5) b.set(0, 16, z, Blocks.SEA_LANTERN.defaultBlockState());
        chest(b, -8, 1, -11, Direction.SOUTH, "kiryu_hangar", r);
        b.fill(-1, 0, 14, 1, 0, 14, Blocks.YELLOW_CONCRETE.defaultBlockState());
    }

    // ------------------------------------------------------------------ the Xilien mothership (Godzilla Final Wars)

    private void xilienMothership(B b, RandomSource r) {
        int radius = 20;
        b.foundation(radius, Blocks.STONE.defaultBlockState());
        for (int dx = -radius; dx <= radius; dx++)
            for (int dz = -radius; dz <= radius; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > radius) continue;
                b.set(dx, 0, dz, Blocks.BLACK_CONCRETE.defaultBlockState());
                int roof = 6 + (int) ((1 - Math.pow(d / radius, 2)) * 6);
                for (int dy = 1; dy < roof; dy++) {
                    boolean wall = d > radius - 1.2;
                    if (wall) {
                        boolean window = dy == 3 && ((int) Math.round(Math.atan2(dz, dx) * 10)) % 3 == 0;
                        b.set(dx, dy, dz, window ? Blocks.RED_STAINED_GLASS.defaultBlockState() : Blocks.GRAY_CONCRETE.defaultBlockState());
                    } else {
                        b.set(dx, dy, dz, Blocks.AIR.defaultBlockState());
                    }
                }
                b.set(dx, roof, dz, d < 4 ? Blocks.RED_STAINED_GLASS.defaultBlockState() : Blocks.BLACK_CONCRETE.defaultBlockState());
            }
        // Doors at the four points
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            int ox = dir.getStepX() * radius, oz = dir.getStepZ() * radius;
            for (int w = -1; w <= 1; w++)
                for (int dy = 1; dy <= 4; dy++) b.set(ox + (oz != 0 ? w : 0), dy, oz + (ox != 0 ? w : 0), Blocks.AIR.defaultBlockState());
        }
        // The control core
        b.fill(-2, 1, -2, 2, 1, 2, Blocks.CRYING_OBSIDIAN.defaultBlockState());
        b.set(0, 2, 0, transmitter(AlienSignal.XILIEN));
        for (int dy = 2; dy <= 4; dy++) {
            b.set(-1, dy, -1, Blocks.RED_STAINED_GLASS.defaultBlockState());
            b.set(1, dy, -1, Blocks.RED_STAINED_GLASS.defaultBlockState());
            b.set(-1, dy, 1, Blocks.RED_STAINED_GLASS.defaultBlockState());
            b.set(1, dy, 1, Blocks.RED_STAINED_GLASS.defaultBlockState());
        }
        for (int i = 0; i < 6; i++) {
            double a = Math.PI * 2 * i / 6;
            b.set((int) (Math.cos(a) * 12), 1, (int) (Math.sin(a) * 12), Blocks.REDSTONE_LAMP.defaultBlockState());
        }
        chest(b, 4, 1, 0, Direction.WEST, "xilien_mothership", r);
        chest(b, -4, 1, 0, Direction.EAST, "xilien_mothership", r);
        // Landing struts
        for (int[] s : new int[][]{{14, 14}, {-14, 14}, {14, -14}, {-14, -14}}) b.fill(s[0], -3, s[1], s[0], -1, s[1], Blocks.IRON_BLOCK.defaultBlockState());
        lair(b, 0, 1, 12, ModEntities.MONSTER_X.get(), -1);
        lair(b, 10, 15, 0, ModEntities.XILIEN_SAUCER.get(), -1);
        lair(b, -10, 15, 0, ModEntities.XILIEN_SAUCER.get(), -1);
    }
}

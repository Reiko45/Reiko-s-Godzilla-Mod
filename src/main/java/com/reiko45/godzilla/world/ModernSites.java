package com.reiko45.godzilla.world;

import com.reiko45.godzilla.block.NanometalBlock;
import com.reiko45.godzilla.registry.ModBlocks;
import com.reiko45.godzilla.registry.ModEntities;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

import static com.reiko45.godzilla.world.ClassicSitePiece.chest;
import static com.reiko45.godzilla.world.ClassicSitePiece.ladder;
import static com.reiko45.godzilla.world.ClassicSitePiece.lair;

/** The Phase 5 (Reiwa and modern) sites, built by {@link ClassicSitePiece}. */
final class ModernSites {
    private ModernSites() {}

    private static BlockState s(net.minecraft.world.level.block.Block block) {
        return block.defaultBlockState();
    }

    /** A demolition charge with a lever on top: flip it and the building above comes down. */
    private static void rigged(ClassicSitePiece.B b, int x, int y, int z) {
        b.set(x, y, z, s(ModBlocks.DEMOLITION_CHARGE.get()));
        b.set(x, y + 1, z, Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE, AttachFace.FLOOR));
    }

    private static void tower(ClassicSitePiece.B b, RandomSource r, int x0, int z0, int w, int h, BlockState wall) {
        for (int y = 1; y <= h; y++) {
            for (int x = x0; x < x0 + w; x++) {
                for (int z = z0; z < z0 + w; z++) {
                    boolean edge = x == x0 || x == x0 + w - 1 || z == z0 || z == z0 + w - 1;
                    boolean floor = y % 4 == 0;
                    if (!edge && !floor) {
                        b.set(x, y, z, s(Blocks.AIR));
                        continue;
                    }
                    if (y > h - 3 && r.nextInt(3) == 0) continue;
                    boolean window = edge && y % 4 == 2 && (x + z) % 2 == 0;
                    b.set(x, y, z, window ? s(Blocks.GLASS_PANE) : wall);
                }
            }
        }
    }

    // ------------------------------------------------------------------ Shin Godzilla

    static void tokyoRuins(ClassicSitePiece.B b, RandomSource r) {
        b.foundation(26, s(Blocks.STONE));
        b.clearAbove(26, 30);
        for (int x = -26; x <= 26; x++)
            for (int z = -26; z <= 26; z++) {
                boolean road = Math.floorMod(x + 2, 12) < 4 || Math.floorMod(z + 2, 12) < 4;
                b.set(x, 0, z, road ? s(Blocks.GRAY_CONCRETE) : s(Blocks.SMOOTH_STONE));
            }
        BlockState[] walls = {s(Blocks.LIGHT_GRAY_CONCRETE), s(Blocks.WHITE_CONCRETE), s(Blocks.GRAY_CONCRETE), s(Blocks.CYAN_TERRACOTTA)};
        for (int gx = -1; gx <= 1; gx++)
            for (int gz = -1; gz <= 1; gz++) {
                if (gx == 0 && gz == 0) continue;
                int x0 = gx * 12 + 3, z0 = gz * 12 + 3;
                int h = 8 + r.nextInt(18);
                tower(b, r, x0, z0, 7, h, walls[r.nextInt(walls.length)]);
                if (r.nextInt(2) == 0) rigged(b, x0 + 3, 1, z0 + 3);
            }
        // The elevated line, with a stretch of track.
        for (int x = -26; x <= 26; x++) {
            b.fill(x, 6, -1, x, 6, 1, s(Blocks.SMOOTH_STONE_SLAB));
            b.set(x, 7, 0, s(Blocks.RAIL));
            if (Math.floorMod(x, 8) == 0) b.fill(x, 1, 0, x, 5, 0, s(Blocks.STONE_BRICKS));
        }
        // Supply dumps of Operation Yashiori in the central plaza.
        b.fill(-4, 1, -4, 4, 1, 4, s(Blocks.AIR));
        chest(b, -2, 1, -2, Direction.SOUTH, "tokyo_ruins", r);
        chest(b, 2, 1, 2, Direction.NORTH, "tokyo_ruins", r);
        b.set(0, 1, 3, s(Blocks.WHITE_CONCRETE));
        b.set(0, 2, 3, s(Blocks.SEA_LANTERN));
    }

    // ------------------------------------------------------------------ Godzilla Minus One / Minus Zero

    static void odoIsland(ClassicSitePiece.B b, RandomSource r) {
        b.foundation(20, s(Blocks.SAND));
        b.clearAbove(18, 12);
        b.fill(-3, 0, -18, 3, 0, 18, s(Blocks.GRAY_CONCRETE));
        for (int z = -16; z <= 16; z += 4) b.set(0, 0, z, s(Blocks.WHITE_CONCRETE));
        // Hangar with a damaged Shinden.
        b.hollow(6, 0, -6, 16, 7, 6, s(Blocks.SPRUCE_PLANKS));
        b.fill(6, 1, -3, 6, 5, 3, s(Blocks.AIR));
        b.fill(7, 0, -5, 15, 0, 5, s(Blocks.STONE));
        lair(b, 11, 1, 0, ModEntities.SHINDEN.get(), -1);
        chest(b, 14, 1, -4, Direction.WEST, "odo_island", r);
        // Bunkers
        for (int[] p : new int[][]{{-12, -10}, {-12, 8}}) {
            b.hollow(p[0] - 2, 0, p[1] - 2, p[0] + 2, 3, p[1] + 2, s(Blocks.STONE_BRICKS));
            b.set(p[0] + 2, 1, p[1], s(Blocks.AIR));
            b.set(p[0] + 2, 2, p[1], s(Blocks.AIR));
        }
        chest(b, -12, 1, 8, Direction.EAST, "odo_island", r);
        // Whatever the islanders feared comes up from the water at night.
        lair(b, -14, 1, 0, ModEntities.MINUS_ONE_GODZILLA.get(), 0);
    }

    static void wadatsumiDock(ClassicSitePiece.B b, RandomSource r) {
        b.foundation(18, s(Blocks.STONE));
        b.fill(-18, 0, -4, 18, 0, 4, s(Blocks.SPRUCE_PLANKS));
        for (int x = -18; x <= 18; x += 4) {
            b.fill(x, -6, -4, x, -1, -4, s(Blocks.SPRUCE_LOG));
            b.fill(x, -6, 4, x, -1, 4, s(Blocks.SPRUCE_LOG));
        }
        // Two destroyer hulls moored alongside.
        for (int side : new int[]{-8, 8}) {
            for (int x = -14; x <= 14; x++) {
                int half = Math.abs(x) > 11 ? 1 : 2;
                b.fill(x, 0, side - half, x, 2, side + half, s(Blocks.GRAY_CONCRETE));
            }
            b.fill(-2, 3, side - 1, 2, 5, side + 1, s(Blocks.LIGHT_GRAY_CONCRETE));
            b.fill(0, 6, side, 0, 9, side, s(Blocks.IRON_BARS));
        }
        // Freon depots and balloon rigs.
        for (int x = -16; x <= -10; x += 3) {
            b.fill(x, 1, 0, x, 3, 0, s(Blocks.LIGHT_BLUE_CONCRETE));
            b.set(x, 4, 0, s(Blocks.IRON_TRAPDOOR));
        }
        for (int x = 10; x <= 16; x += 3) b.fill(x, 1, -1, x, 3, 1, s(Blocks.YELLOW_WOOL));
        chest(b, -6, 1, -2, Direction.SOUTH, "wadatsumi_dock", r);
        chest(b, 6, 1, 2, Direction.NORTH, "wadatsumi_dock", r);
    }

    static void matsumoto(ClassicSitePiece.B b, RandomSource r) {
        b.foundation(22, s(Blocks.DIRT));
        b.clearAbove(22, 20);
        b.disc(22, 0, d -> d < 9 ? s(Blocks.STONE_BRICKS) : s(Blocks.GRASS_BLOCK));
        // The castle keep: black and white tiers.
        int[][] tiers = {{6, 4}, {5, 4}, {4, 3}, {3, 3}};
        int y = 1;
        for (int[] t : tiers) {
            b.hollow(-t[0], y, -t[0], t[0], y + t[1], t[0], s(Blocks.BLACK_CONCRETE));
            b.fill(-t[0], y + 1, -t[0], t[0], y + 1, t[0], s(Blocks.WHITE_CONCRETE));
            b.fill(-t[0] + 1, y + 1, -t[0] + 1, t[0] - 1, y + 1, t[0] - 1, s(Blocks.AIR));
            y += t[1];
        }
        b.fill(-1, 1, -6, 1, 3, -6, s(Blocks.AIR));
        b.set(0, y, 0, s(ModBlocks.STORM_BEACON.get()));
        b.set(1, y, 0, Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE, AttachFace.FLOOR));
        chest(b, 2, 1, 2, Direction.WEST, "matsumoto", r);
        // Town houses around the castle.
        for (int i = 0; i < 8; i++) {
            double a = Math.PI * 2 * i / 8 + 0.3;
            int hx = (int) (Math.cos(a) * 16), hz = (int) (Math.sin(a) * 16);
            b.hollow(hx - 2, 0, hz - 2, hx + 2, 3, hz + 2, s(Blocks.OAK_PLANKS));
            b.fill(hx - 2, 4, hz - 2, hx + 2, 4, hz + 2, s(Blocks.DARK_OAK_PLANKS));
            b.set(hx, 1, hz - 2, s(Blocks.AIR));
            b.set(hx, 2, hz - 2, s(Blocks.AIR));
        }
    }

    // ------------------------------------------------------------------ the anime trilogy

    static void mechagodzillaCity(ClassicSitePiece.B b, RandomSource r) {
        BlockState active = ModBlocks.NANOMETAL.get().defaultBlockState().setValue(NanometalBlock.ACTIVE, true);
        BlockState inert = ModBlocks.NANOMETAL.get().defaultBlockState();
        b.foundation(26, inert);
        b.clearAbove(26, 32);
        b.disc(26, 0, d -> d % 5 == 0 ? active : inert);
        b.disc(26, -1, d -> inert);
        // Rings of spike towers.
        for (int ring : new int[]{10, 18, 24}) {
            int n = ring / 2;
            for (int i = 0; i < n; i++) {
                double a = Math.PI * 2 * i / n + ring;
                int x = (int) (Math.cos(a) * ring), z = (int) (Math.sin(a) * ring);
                int h = 6 + r.nextInt(ring == 10 ? 14 : 8);
                b.fill(x, 1, z, x, h, z, i % 3 == 0 ? active : inert);
                if (r.nextBoolean()) b.fill(x + 1, 1, z, x + 1, h / 2, z, inert);
            }
        }
        // The liquid nanometal basin.
        for (int i = 0; i < 64; i++) {
            double a = Math.PI * 2 * i / 64;
            b.set((int) (Math.cos(a) * 14), 1, (int) (Math.sin(a) * 14), s(ModBlocks.LIQUID_NANOMETAL.get()));
        }
        // The central tower and its core: Mechagodzilla's head.
        b.hollow(-4, 0, -4, 4, 16, 4, inert);
        b.fill(-1, 1, -4, 1, 3, -4, s(Blocks.AIR));
        for (int y = 1; y <= 15; y++) b.set(0, y, 3, ladder(Direction.NORTH));
        b.set(0, 16, 3, s(Blocks.AIR));
        b.fill(-3, 16, -3, 3, 16, 3, active);
        lair(b, 0, 17, 0, ModEntities.MECHAGODZILLA_CITY.get(), -1);
        chest(b, 2, 1, 2, Direction.WEST, "mechagodzilla_city", r);
        // Vulture hangars.
        for (int i = 0; i < 3; i++) {
            double a = Math.PI * 2 * i / 3 + 0.5;
            int x = (int) (Math.cos(a) * 20), z = (int) (Math.sin(a) * 20);
            b.hollow(x - 2, 0, z - 2, x + 2, 4, z + 2, inert);
            b.fill(x - 1, 1, z - 2, x + 1, 3, z - 2, s(Blocks.AIR));
            lair(b, x, 1, z, ModEntities.VULTURE.get(), -1);
        }
    }

    static void houtuaVillage(ClassicSitePiece.B b, RandomSource r) {
        b.foundation(20, s(Blocks.DIRT));
        b.clearAbove(20, 14);
        b.disc(20, 0, d -> d < 6 ? s(Blocks.MUD_BRICKS) : s(Blocks.MOSS_BLOCK));
        for (int i = 0; i < 90; i++) {
            int x = r.nextInt(41) - 20, z = r.nextInt(41) - 20;
            if (x * x + z * z > 400 || x * x + z * z < 49) continue;
            b.set(x, 1, z, s(ModBlocks.METALLIC_FROND.get()));
        }
        // Metal-barked trees of the far-future forest.
        for (int i = 0; i < 7; i++) {
            double a = Math.PI * 2 * i / 7;
            int x = (int) (Math.cos(a) * 17), z = (int) (Math.sin(a) * 17);
            int h = 7 + r.nextInt(5);
            b.fill(x, 1, z, x, h, z, s(Blocks.WAXED_OXIDIZED_COPPER));
            b.fill(x - 2, h, z - 2, x + 2, h, z + 2, s(Blocks.WARPED_WART_BLOCK));
        }
        // Huts and the moth totems.
        for (int i = 0; i < 5; i++) {
            double a = Math.PI * 2 * i / 5 + 0.6;
            int x = (int) (Math.cos(a) * 10), z = (int) (Math.sin(a) * 10);
            b.hollow(x - 2, 0, z - 2, x + 2, 3, z + 2, s(Blocks.PACKED_MUD));
            b.fill(x - 2, 4, z - 2, x + 2, 4, z + 2, s(Blocks.HAY_BLOCK));
            b.set(x, 1, z - 2, s(Blocks.AIR));
            b.set(x, 2, z - 2, s(Blocks.AIR));
        }
        for (int[] p : new int[][]{{-3, 0}, {3, 0}}) {
            b.fill(p[0], 1, p[1], p[0], 5, p[1], s(Blocks.STRIPPED_DARK_OAK_LOG));
            b.set(p[0], 6, p[1], s(Blocks.YELLOW_WOOL));
            b.set(p[0] - 1, 5, p[1], s(Blocks.ORANGE_WOOL));
            b.set(p[0] + 1, 5, p[1], s(Blocks.ORANGE_WOOL));
        }
        b.set(0, 1, 0, s(Blocks.SHROOMLIGHT));
        chest(b, 0, 1, 2, Direction.NORTH, "houtua_village", r);
        lair(b, 16, 1, -6, ModEntities.SERVUM.get(), -1);
        lair(b, -16, 1, 8, ModEntities.SERVUM.get(), -1);
    }

    static void exifObservatory(ClassicSitePiece.B b, RandomSource r) {
        b.foundation(14, s(Blocks.STONE));
        b.clearAbove(14, 18);
        b.disc(13, 0, d -> d < 4 ? s(Blocks.CHISELED_QUARTZ_BLOCK) : d % 3 == 0 ? s(Blocks.GOLD_BLOCK) : s(Blocks.QUARTZ_BRICKS));
        for (int i = 0; i < 8; i++) {
            double a = Math.PI * 2 * i / 8;
            int x = (int) (Math.cos(a) * 11), z = (int) (Math.sin(a) * 11);
            int h = 4 + r.nextInt(10);
            b.fill(x, 1, z, x, h, z, s(Blocks.QUARTZ_PILLAR));
            if (h > 10) b.set(x, h + 1, z, s(Blocks.GOLD_BLOCK));
        }
        b.fill(-1, 1, -1, 1, 1, 1, s(Blocks.QUARTZ_BRICKS));
        b.set(0, 2, 0, s(ModBlocks.GARBETRIUM_TOTEM.get()));
        for (int[] p : new int[][]{{-3, -3}, {3, 3}, {-3, 3}, {3, -3}}) b.set(p[0], 1, p[1], s(Blocks.SOUL_LANTERN));
        chest(b, 0, 1, 6, Direction.NORTH, "exif_observatory", r);
    }

    // ------------------------------------------------------------------ Godzilla Singular Point

    static void otakiFactory(ClassicSitePiece.B b, RandomSource r) {
        b.foundation(16, s(Blocks.STONE));
        b.clearAbove(16, 24);
        b.fill(-16, 0, -16, 16, 0, 16, s(Blocks.GRAY_CONCRETE));
        b.hollow(-10, 0, -8, 4, 8, 8, s(Blocks.BRICKS));
        b.fill(-10, 1, -2, -10, 6, 2, s(Blocks.AIR));
        b.fill(-9, 0, -7, 3, 0, 7, s(Blocks.SMOOTH_STONE));
        // Jet Jaguar's gantry.
        b.fill(-5, 1, -3, -5, 7, -3, s(Blocks.IRON_BARS));
        b.fill(-5, 1, 3, -5, 7, 3, s(Blocks.IRON_BARS));
        b.fill(-5, 7, -3, -5, 7, 3, s(Blocks.IRON_BLOCK));
        lair(b, -3, 1, 0, ModEntities.JET_JAGUAR_PP.get(), 0);
        chest(b, 2, 1, -6, Direction.SOUTH, "otaki_factory", r);
        // The Misakioku antenna.
        b.fill(10, 1, 10, 10, 22, 10, s(Blocks.IRON_BARS));
        for (int y = 6; y <= 20; y += 7) {
            b.fill(8, y, 10, 12, y, 10, s(Blocks.IRON_BARS));
            b.fill(10, y, 8, 10, y, 12, s(Blocks.IRON_BARS));
        }
        b.set(10, 23, 10, s(Blocks.LIGHTNING_ROD));
        b.hollow(12, 0, 4, 16, 4, 8, s(Blocks.WHITE_TERRACOTTA));
        chest(b, 14, 1, 6, Direction.WEST, "otaki_factory", r);
    }

    static void shivaFacility(ClassicSitePiece.B b, RandomSource r, int surfaceDy) {
        b.hollow(-17, 0, -17, 17, 11, 17, s(Blocks.WHITE_CONCRETE));
        b.fill(-16, 0, -16, 16, 0, 16, s(Blocks.SMOOTH_STONE));
        for (int x = -15; x <= 15; x += 6) b.fill(x, 11, -16, x, 11, 16, s(Blocks.SEA_LANTERN));
        // The Red Dust pool and Salunga's cage over it.
        b.fill(-6, -3, -6, 6, -1, 6, s(Blocks.RED_CONCRETE));
        b.fill(-5, -2, -5, 5, 0, 5, s(Blocks.AIR));
        for (int x = -5; x <= 5; x++)
            for (int z = -5; z <= 5; z++) b.set(x, -2, z, s(ModBlocks.RED_DUST.get()));
        for (int x = -7; x <= 7; x++)
            for (int z = -7; z <= 7; z++)
                if (Math.abs(x) == 7 || Math.abs(z) == 7) b.fill(x, 1, z, x, 9, z, s(Blocks.IRON_BARS));
        lair(b, 0, -1, 0, ModEntities.SALUNGA.get(), -1);
        // Crystal samples and the lab.
        for (int[] p : new int[][]{{-14, -14}, {14, -14}, {-14, 14}, {14, 14}, {-12, 0}, {12, 0}}) b.set(p[0], 1, p[1], s(ModBlocks.ARCHETYPE_CRYSTAL.get()));
        b.fill(10, 1, -12, 14, 1, -10, s(Blocks.IRON_BLOCK));
        chest(b, 12, 2, -11, Direction.SOUTH, "shiva_facility", r);
        chest(b, -12, 1, 12, Direction.EAST, "shiva_facility", r);
        // Lift shaft up to the surface.
        for (int y = 1; y <= Math.max(12, surfaceDy + 1); y++) {
            b.set(-15, y, -15, s(Blocks.AIR));
            b.set(-15, y, -16, ladder(Direction.SOUTH));
            if (y > 11) {
                b.set(-16, y, -15, s(Blocks.WHITE_CONCRETE));
                b.set(-14, y, -15, s(Blocks.WHITE_CONCRETE));
                b.set(-15, y, -14, s(Blocks.WHITE_CONCRETE));
            }
        }
    }

    // ------------------------------------------------------------------ Monarch: Legacy of Monsters

    static void apexLab(ClassicSitePiece.B b, RandomSource r) {
        b.foundation(18, s(Blocks.STONE));
        b.clearAbove(18, 14);
        b.fill(-18, 0, -18, 18, 0, 18, s(Blocks.GRAY_CONCRETE));
        b.hollow(-12, 0, -10, 12, 9, 10, s(Blocks.LIGHT_GRAY_CONCRETE));
        b.fill(-12, 1, -2, -12, 4, 2, s(Blocks.AIR));
        for (int x = -10; x <= 10; x += 5) b.set(x, 9, 0, s(Blocks.SEA_LANTERN));
        // The egg vault.
        b.hollow(4, 0, -4, 10, 5, 4, s(Blocks.IRON_BLOCK));
        b.fill(4, 1, -1, 4, 3, 1, s(Blocks.GLASS));
        b.set(7, 1, 0, s(ModBlocks.TITAN_X_EGG.get()));
        b.fill(6, 0, -1, 8, 0, 1, s(Blocks.WATER));
        b.set(7, 0, 0, s(Blocks.SEA_LANTERN));
        // Specimen pens.
        for (int z = -8; z <= 8; z += 4) b.fill(-10, 1, z, -4, 3, z, s(Blocks.IRON_BARS));
        chest(b, -8, 1, -6, Direction.SOUTH, "apex_lab", r);
        chest(b, 0, 1, 8, Direction.NORTH, "apex_lab", r);
        lair(b, 16, 1, 0, ModEntities.TITAN_X.get(), 1);
    }

    static void santaSoledad(ClassicSitePiece.B b, RandomSource r) {
        b.foundation(14, s(Blocks.STONE));
        b.clearAbove(14, 12);
        b.disc(9, 0, d -> d < 3 ? s(Blocks.PRISMARINE_BRICKS) : s(Blocks.STONE_BRICKS));
        for (int i = 0; i < 6; i++) {
            double a = Math.PI * 2 * i / 6;
            int x = (int) (Math.cos(a) * 7), z = (int) (Math.sin(a) * 7);
            b.fill(x, 1, z, x, 4, z, s(Blocks.STONE_BRICK_WALL));
            b.set(x, 5, z, s(Blocks.SEA_LANTERN));
        }
        // The offering altar to "El Gran Dios del Mar".
        b.fill(-1, 1, -1, 1, 1, 1, s(Blocks.DARK_PRISMARINE));
        b.set(0, 2, 0, s(Blocks.CONDUIT));
        for (int[] p : new int[][]{{-1, -1}, {1, 1}, {-1, 1}, {1, -1}}) b.set(p[0], 2, p[1], s(Blocks.CANDLE));
        chest(b, 0, 1, 4, Direction.NORTH, "santa_soledad", r);
        lair(b, 0, 1, -12, ModEntities.TITAN_X.get(), 0);
    }

    static void outpost88(ClassicSitePiece.B b, RandomSource r) {
        b.foundation(14, s(Blocks.PACKED_ICE));
        b.clearAbove(14, 12);
        b.disc(14, 0, d -> s(Blocks.SNOW_BLOCK));
        b.hollow(-8, 0, -6, 2, 6, 6, s(Blocks.WHITE_CONCRETE));
        b.fill(-8, 1, -1, -8, 3, 1, s(Blocks.AIR));
        b.hollow(4, 0, -4, 10, 5, 4, s(Blocks.LIGHT_GRAY_CONCRETE));
        b.fill(4, 1, 0, 4, 2, 0, s(Blocks.AIR));
        // The sealed rift.
        b.fill(-2, 0, 8, 2, 0, 12, s(Blocks.CRYING_OBSIDIAN));
        b.fill(-1, 0, 9, 1, 0, 11, s(Blocks.BLUE_ICE));
        // The self-destruct: charges under the outpost and a console lever.
        rigged(b, -3, 1, 0);
        rigged(b, 7, 1, 0);
        chest(b, -6, 1, 4, Direction.NORTH, "outpost_88", r);
        lair(b, 0, 1, 10, ModEntities.FROST_VARK.get(), -1);
    }
}

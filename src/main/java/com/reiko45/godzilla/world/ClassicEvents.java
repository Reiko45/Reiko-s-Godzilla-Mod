package com.reiko45.godzilla.world;

import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.kaiju.MechagodzillaEntity;
import com.reiko45.godzilla.entity.millennium.MillenniumGodzillaEntity;
import com.reiko45.godzilla.entity.showa.*;
import com.reiko45.godzilla.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Phase 4 events: the Space Hunter Nebula M's attack (Gigan and King Ghidorah), Megalon bursting out of the ground,
 * Hedorah drifting in, a Xilien invasion, Titanosaurus and Mechagodzilla 2, the Millennian UFO, a Meganulon swarm,
 * GMK Godzilla coming ashore, and a Kamacuras pack.
 */
final class ClassicEvents {
    private ClassicEvents() {}

    @Nullable
    private static BlockPos findLand(ServerLevel level, ServerPlayer player, int min, int max) {
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < 40; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = min + random.nextDouble() * (max - min);
            int x = (int) (player.getX() + Math.cos(angle) * dist), z = (int) (player.getZ() + Math.sin(angle) * dist);
            if (!level.hasChunkAt(new BlockPos(x, 0, z))) continue;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            if (level.getFluidState(pos.below()).isEmpty() && level.getFluidState(pos).isEmpty()) return pos;
        }
        return null;
    }

    @Nullable
    private static BlockPos findSea(ServerLevel level, ServerPlayer player, int min, int max, int depth) {
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < 32; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = min + random.nextDouble() * (max - min);
            int x = (int) (player.getX() + Math.cos(angle) * dist), z = (int) (player.getZ() + Math.sin(angle) * dist);
            BlockPos column = new BlockPos(x, level.getSeaLevel() - 1, z);
            if (!level.hasChunkAt(column) || !level.getBiome(column).is(BiomeTags.IS_OCEAN)) continue;
            int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
            if (level.getSeaLevel() - floor < depth || !level.getFluidState(column).is(FluidTags.WATER)) continue;
            return new BlockPos(x, floor, z);
        }
        return null;
    }

    @Nullable
    private static <T extends Mob> T spawn(ServerLevel level, EntityType<T> type, double x, double y, double z, ServerPlayer facing, int variant) {
        T mob = type.create(level);
        if (mob == null) return null;
        mob.moveTo(x + 0.5, y, z + 0.5, (float) Math.toDegrees(Math.atan2(facing.getZ() - z, facing.getX() - x)) - 90f, 0);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.EVENT, null);
        if (variant >= 0 && mob instanceof KaijuEntity k) {
            k.setVariant(variant);
            k.setHealth(k.getMaxHealth());
        }
        level.addFreshEntity(mob);
        return mob;
    }

    private static void announce(ServerLevel level, ServerPlayer near, String key, ChatFormatting color) {
        for (ServerPlayer p : level.players()) {
            if (p.distanceToSqr(near) < 512 * 512) p.sendSystemMessage(Component.translatable(key).withStyle(color, ChatFormatting.ITALIC));
        }
    }

    private static void eruption(ServerLevel level, BlockPos pos) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()), pos.getX(), pos.getY() + 1, pos.getZ(), 200, 4, 2, 4, 0.3);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.getX(), pos.getY() + 2, pos.getZ(), 1, 0, 0, 0, 0);
    }

    /** The Space Hunter Nebula M send Gigan and King Ghidorah down from space, wrapped in crystal. */
    static boolean gigan(ServerLevel level, ServerPlayer player) {
        BlockPos land = findLand(level, player, 60, 110);
        if (land == null) return false;
        KaijuEntity gigan = spawn(level, ModEntities.GIGAN.get(), land.getX(), land.getY() + 40, land.getZ(), player, -1);
        if (gigan instanceof FlyingKaijuEntity f) f.setFlying(true);
        if (gigan != null) {
            gigan.setDestination(player.blockPosition());
            level.sendParticles(ParticleTypes.END_ROD, gigan.getX(), gigan.getY(), gigan.getZ(), 80, 2, 4, 2, 0.1);
        }
        BlockPos other = land.offset(level.getRandom().nextInt(41) - 20, 0, level.getRandom().nextInt(41) - 20);
        KaijuEntity ghidorah = spawn(level, ModEntities.KING_GHIDORAH.get(), other.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, other.getX(), other.getZ()) + 30,
                other.getZ(), player, -1);
        if (ghidorah != null) ghidorah.setDestination(player.blockPosition());
        announce(level, player, "event.reikogodzilla.gigan", ChatFormatting.LIGHT_PURPLE);
        return gigan != null;
    }

    /** Seatopia sends its god: Megalon drills up out of the ground. */
    static boolean megalon(ServerLevel level, ServerPlayer player) {
        BlockPos land = findLand(level, player, 40, 80);
        if (land == null) return false;
        eruption(level, land);
        KaijuEntity megalon = spawn(level, ModEntities.MEGALON.get(), land.getX(), land.getY(), land.getZ(), player, -1);
        if (megalon == null) return false;
        megalon.setDestination(player.blockPosition());
        announce(level, player, "event.reikogodzilla.megalon", ChatFormatting.GOLD);
        return true;
    }

    /** Hedorah drifts in on the smog in its flying form. */
    static boolean hedorah(ServerLevel level, ServerPlayer player) {
        BlockPos land = findLand(level, player, 60, 100);
        if (land == null) return false;
        HedorahEntity hedorah = spawn(level, ModEntities.HEDORAH.get(), land.getX(), land.getY() + 25, land.getZ(), player, -1);
        if (hedorah == null) return false;
        hedorah.setDestination(player.blockPosition());
        announce(level, player, "event.reikogodzilla.hedorah", ChatFormatting.DARK_GREEN);
        return true;
    }

    /** Xilien saucers arrive with the monsters they control, or, rarely, with Monster X himself. */
    static boolean xilienInvasion(ServerLevel level, ServerPlayer player) {
        BlockPos land = findLand(level, player, 70, 110);
        if (land == null) return false;
        RandomSource random = level.getRandom();
        for (int i = 0; i < 3; i++) {
            KaijuEntity saucer = spawn(level, ModEntities.XILIEN_SAUCER.get(), land.getX() + random.nextInt(21) - 10, land.getY() + 20 + random.nextInt(8),
                    land.getZ() + random.nextInt(21) - 10, player, -1);
            if (saucer instanceof FlyingKaijuEntity f) f.setFlying(true);
        }
        if (random.nextInt(4) == 0) {
            KaijuEntity x = spawn(level, ModEntities.MONSTER_X.get(), land.getX(), land.getY(), land.getZ(), player, -1);
            if (x != null) x.setDestination(player.blockPosition());
            announce(level, player, "event.reikogodzilla.monster_x", ChatFormatting.RED);
            return true;
        }
        List<Supplier<? extends EntityType<? extends KaijuEntity>>> monsters = List.of(ModEntities.GIGAN, ModEntities.KAMACURAS, ModEntities.KUMONGA,
                ModEntities.KING_CAESAR, ModEntities.ZILLA, ModEntities.ANGUIRUS, ModEntities.RODAN, ModEntities.EBIRAH);
        int count = 2 + random.nextInt(2);
        for (int i = 0; i < count; i++) {
            EntityType<? extends KaijuEntity> type = monsters.get(random.nextInt(monsters.size())).get();
            BlockPos p = land.offset(random.nextInt(31) - 15, 0, random.nextInt(31) - 15);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, p.getX(), p.getZ());
            boolean finalWars = type == ModEntities.GIGAN.get() || type == ModEntities.KAMACURAS.get() || type == ModEntities.KUMONGA.get()
                    || type == ModEntities.KING_CAESAR.get() || type == ModEntities.EBIRAH.get();
            KaijuEntity k = spawn(level, type, p.getX(), y, p.getZ(), player, finalWars ? 1 : -1);
            if (k != null) k.setDestination(player.blockPosition());
        }
        announce(level, player, "event.reikogodzilla.xilien_invasion", ChatFormatting.RED);
        return true;
    }

    /** Titanosaurus rises from the sea with Mechagodzilla 2 relaying the Black Hole aliens' orders. */
    static boolean titanosaurus(ServerLevel level, ServerPlayer player) {
        BlockPos sea = findSea(level, player, 70, 110, 8);
        if (sea == null) return false;
        KaijuEntity titan = spawn(level, ModEntities.TITANOSAURUS.get(), sea.getX(), sea.getY(), sea.getZ(), player, -1);
        if (titan == null) return false;
        titan.setDestination(player.blockPosition());
        BlockPos land = findLand(level, player, 50, 90);
        if (land != null) {
            KaijuEntity mg2 = spawn(level, ModEntities.MECHAGODZILLA.get(), land.getX(), land.getY(), land.getZ(), player, MechagodzillaEntity.MECHAGODZILLA_2);
            if (mg2 != null) mg2.setDestination(player.blockPosition());
        }
        announce(level, player, "event.reikogodzilla.titanosaurus", ChatFormatting.DARK_AQUA);
        return true;
    }

    /** The Millennian UFO lifts off the sea floor. */
    static boolean orga(ServerLevel level, ServerPlayer player) {
        BlockPos sea = findSea(level, player, 60, 100, 10);
        if (sea == null) return false;
        KaijuEntity ufo = spawn(level, ModEntities.MILLENNIAN_UFO.get(), sea.getX(), level.getSeaLevel() + 4, sea.getZ(), player, -1);
        if (ufo == null) return false;
        if (ufo instanceof FlyingKaijuEntity f) f.setFlying(true);
        ufo.setDestination(player.blockPosition());
        level.sendParticles(ParticleTypes.SPLASH, ufo.getX(), level.getSeaLevel(), ufo.getZ(), 200, 6, 1, 6, 0.4);
        announce(level, player, "event.reikogodzilla.orga", ChatFormatting.GOLD);
        return true;
    }

    /** Meganulon crawl out of the water and Meganula swarm overhead. */
    static boolean meganulon(ServerLevel level, ServerPlayer player) {
        BlockPos land = findLand(level, player, 24, 48);
        if (land == null) return false;
        RandomSource random = level.getRandom();
        for (int i = 0; i < 4; i++) {
            BlockPos p = land.offset(random.nextInt(9) - 4, 0, random.nextInt(9) - 4);
            spawn(level, ModEntities.MEGANULON.get(), p.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, p.getX(), p.getZ()), p.getZ(), player, -1);
        }
        for (int i = 0; i < 4; i++) {
            spawn(level, ModEntities.MEGANULA.get(), land.getX() + random.nextInt(9) - 4, land.getY() + 6 + random.nextInt(4), land.getZ() + random.nextInt(9) - 4, player, -1);
        }
        announce(level, player, "event.reikogodzilla.meganulon", ChatFormatting.DARK_GREEN);
        return true;
    }

    /** GMK Godzilla comes ashore, and the Guardians stir. */
    static boolean gmkGodzilla(ServerLevel level, ServerPlayer player) {
        BlockPos sea = findSea(level, player, 80, 120, 8);
        if (sea == null) return false;
        KaijuEntity g = spawn(level, ModEntities.MILLENNIUM_GODZILLA.get(), sea.getX(), sea.getY(), sea.getZ(), player, MillenniumGodzillaEntity.GMK);
        if (g == null) return false;
        g.setDestination(player.blockPosition());
        announce(level, player, "event.reikogodzilla.gmk_godzilla", ChatFormatting.DARK_RED);
        return true;
    }

    /** A pack of three Kamacuras. */
    static boolean kamacuras(ServerLevel level, ServerPlayer player) {
        BlockPos land = findLand(level, player, 40, 70);
        if (land == null) return false;
        for (int i = 0; i < 3; i++) {
            KaijuEntity k = spawn(level, ModEntities.KAMACURAS.get(), land.getX() + i * 6 - 6, land.getY() + 8, land.getZ(), player, -1);
            if (k != null) k.setDestination(player.blockPosition());
        }
        announce(level, player, "event.reikogodzilla.kamacuras", ChatFormatting.GREEN);
        return true;
    }
}

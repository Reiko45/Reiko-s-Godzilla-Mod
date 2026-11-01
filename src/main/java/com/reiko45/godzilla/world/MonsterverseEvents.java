package com.reiko45.godzilla.world;

import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.monsterverse.MutoEntity;
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

/** Phase 3 events: MonsterVerse Titans surfacing, MUTOs, Scylla, Behemoth, the Alpha Call, Skullcrawler swarms. */
final class MonsterverseEvents {
    private MonsterverseEvents() {}

    /** A dry spot {@code min}-{@code max} blocks from the player. */
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

    private static <T extends Mob> T spawn(ServerLevel level, EntityType<T> type, double x, double y, double z, ServerPlayer facing) {
        T mob = type.create(level);
        if (mob == null) return null;
        mob.moveTo(x + 0.5, y, z + 0.5, (float) Math.toDegrees(Math.atan2(facing.getZ() - z, facing.getX() - x)) - 90f, 0);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.EVENT, null);
        return mob;
    }

    /** Ground bursting open where something climbs out. */
    private static void eruption(ServerLevel level, BlockPos pos) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()), pos.getX(), pos.getY() + 1, pos.getZ(), 200, 4, 2, 4, 0.3);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.getX(), pos.getY() + 2, pos.getZ(), 1, 0, 0, 0, 0);
    }

    private static void announce(ServerLevel level, ServerPlayer near, String key, ChatFormatting color) {
        for (ServerPlayer p : level.players()) {
            if (p.distanceToSqr(near) < 512 * 512) p.sendSystemMessage(Component.translatable(key).withStyle(color, ChatFormatting.ITALIC));
        }
    }

    static boolean godzilla(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < 32; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = 80 + random.nextDouble() * 50;
            int x = (int) (player.getX() + Math.cos(angle) * dist), z = (int) (player.getZ() + Math.sin(angle) * dist);
            BlockPos column = new BlockPos(x, level.getSeaLevel() - 1, z);
            if (!level.hasChunkAt(column) || !level.getBiome(column).is(BiomeTags.IS_OCEAN)) continue;
            int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
            if (level.getSeaLevel() - floor < 10 || !level.getFluidState(column).is(FluidTags.WATER)) continue;
            KaijuEntity g = spawn(level, ModEntities.MONSTERVERSE_GODZILLA.get(), x, floor, z, player);
            if (g == null) return false;
            g.setDestination(player.blockPosition());
            level.addFreshEntity(g);
            announce(level, player, "event.reikogodzilla.monsterverse_godzilla", ChatFormatting.DARK_AQUA);
            return true;
        }
        return false;
    }

    /** Godzilla (2014): a female bursts out of the ground, and her mate answers her call. */
    static boolean mutos(ServerLevel level, ServerPlayer player) {
        BlockPos land = findLand(level, player, 70, 110);
        if (land == null) return false;
        MutoEntity female = spawn(level, ModEntities.MUTO.get(), land.getX(), land.getY(), land.getZ(), player);
        if (female == null) return false;
        female.setVariant(MutoEntity.FEMALE);
        female.setHealth(female.getMaxHealth());
        female.setDestination(player.blockPosition());
        level.addFreshEntity(female);
        eruption(level, land);
        double a = level.getRandom().nextDouble() * Math.PI * 2;
        MutoEntity male = spawn(level, ModEntities.MUTO.get(), player.getX() + Math.cos(a) * 140, Math.min(level.getMaxBuildHeight() - 20, player.getY() + 60),
                player.getZ() + Math.sin(a) * 140, player);
        if (male != null) {
            male.setVariant(MutoEntity.MALE);
            male.setHealth(male.getMaxHealth());
            male.setFlying(true);
            male.setDestination(land);
            level.addFreshEntity(male);
        }
        announce(level, player, "event.reikogodzilla.muto", ChatFormatting.DARK_GREEN);
        return true;
    }

    static boolean scylla(ServerLevel level, ServerPlayer player) {
        BlockPos land = findLand(level, player, 70, 110);
        if (land == null) return false;
        KaijuEntity scylla = spawn(level, ModEntities.SCYLLA.get(), land.getX(), land.getY(), land.getZ(), player);
        if (scylla == null) return false;
        scylla.setDestination(player.blockPosition());
        level.addFreshEntity(scylla);
        eruption(level, land);
        announce(level, player, "event.reikogodzilla.scylla", ChatFormatting.DARK_GRAY);
        return true;
    }

    static boolean behemoth(ServerLevel level, ServerPlayer player) {
        BlockPos land = findLand(level, player, 80, 120);
        if (land == null) return false;
        KaijuEntity behemoth = spawn(level, ModEntities.BEHEMOTH.get(), land.getX(), land.getY(), land.getZ(), player);
        if (behemoth == null) return false;
        behemoth.setDestination(player.blockPosition());
        level.addFreshEntity(behemoth);
        announce(level, player, "event.reikogodzilla.behemoth", ChatFormatting.DARK_GREEN);
        return true;
    }

    /** King of the Monsters: Monster Zero arrives, and somewhere a Fire Demon is already stirring. */
    static boolean alphaCall(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.getRandom();
        double a = random.nextDouble() * Math.PI * 2;
        double y = Math.min(level.getMaxBuildHeight() - 20, player.getY() + 70);
        KaijuEntity ghidorah = spawn(level, ModEntities.MONSTERVERSE_KING_GHIDORAH.get(), player.getX() + Math.cos(a) * 130, y, player.getZ() + Math.sin(a) * 130, player);
        if (ghidorah == null) return false;
        if (ghidorah instanceof FlyingKaijuEntity f) f.setFlying(true);
        ghidorah.setDestination(player.blockPosition());
        level.addFreshEntity(ghidorah);
        KaijuEntity rodan = spawn(level, ModEntities.MONSTERVERSE_RODAN.get(), player.getX() - Math.cos(a) * 180, y, player.getZ() - Math.sin(a) * 180, player);
        if (rodan != null) {
            if (rodan instanceof FlyingKaijuEntity f) f.setFlying(true);
            level.addFreshEntity(rodan);
        }
        level.setWeatherParameters(0, 3600, true, true);
        announce(level, player, "event.reikogodzilla.alpha_call", ChatFormatting.GOLD);
        return true;
    }

    /** Kong: Skull Island - the crawlers come up from below. */
    static boolean skullcrawlers(ServerLevel level, ServerPlayer player) {
        BlockPos land = findLand(level, player, 20, 36);
        if (land == null) return false;
        int count = 4 + level.getRandom().nextInt(3);
        for (int i = 0; i < count; i++) {
            BlockPos p = land.offset(level.getRandom().nextInt(7) - 3, 0, level.getRandom().nextInt(7) - 3);
            p = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, p);
            Mob crawler = spawn(level, ModEntities.SKULLCRAWLER.get(), p.getX(), p.getY(), p.getZ(), player);
            if (crawler == null) continue;
            crawler.setTarget(player);
            level.addFreshEntity(crawler);
        }
        eruption(level, land);
        announce(level, player, "event.reikogodzilla.skullcrawlers", ChatFormatting.DARK_RED);
        return true;
    }
}

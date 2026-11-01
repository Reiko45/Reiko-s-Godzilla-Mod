package com.reiko45.godzilla.world;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.block.RedDustBlock;
import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.reiwa.*;
import com.reiko45.godzilla.entity.showa.GiganEntity;
import com.reiko45.godzilla.registry.ModBlocks;
import com.reiko45.godzilla.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;

import static com.reiko45.godzilla.world.ClassicEvents.*;

/**
 * Phase 5 events: Shin Godzilla's 2nd form coming ashore, Minus One Godzilla's raid, Minus Zero Ghidorah riding a
 * storm front, tremors in the metallic forest (Servum, rarely Filius), the Singular Point Catastrophe, Titan X's
 * migration, a Gigan Rex invasion, the Planet Eater, a Monarch Titan sighting, the Kraken, and (only with the config
 * switch on) the placeholder MonsterVerse SpaceGodzilla.
 */
public final class ModernEvents {
    private ModernEvents() {}

    static boolean shinGodzilla(ServerLevel level, ServerPlayer player) {
        BlockPos sea = findSea(level, player, 50, 110, 4);
        if (sea == null) return false;
        ShinGodzillaEntity shin = spawn(level, ModEntities.SHIN_GODZILLA.get(), sea.getX(), sea.getY(), sea.getZ(), player, ShinGodzillaEntity.SECOND);
        if (shin == null) return false;
        shin.setDestination(player.blockPosition());
        level.sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.55f, 0.05f, 0.05f), 3f),
                sea.getX(), level.getSeaLevel(), sea.getZ(), 200, 6, 1, 6, 0);
        announce(level, player, "event.reikogodzilla.shin_godzilla", ChatFormatting.RED);
        return true;
    }

    static boolean ginzaRaid(ServerLevel level, ServerPlayer player) {
        BlockPos sea = findSea(level, player, 60, 110, 6);
        if (sea == null) return false;
        MinusOneGodzillaEntity g = spawn(level, ModEntities.MINUS_ONE_GODZILLA.get(), sea.getX(), sea.getY(), sea.getZ(), player, MinusOneGodzillaEntity.MINUS_ONE);
        if (g == null) return false;
        g.setDestination(player.blockPosition());
        announce(level, player, "event.reikogodzilla.ginza_raid", ChatFormatting.DARK_AQUA);
        return true;
    }

    static boolean ghidorahStorm(ServerLevel level, ServerPlayer player) {
        BlockPos land = findLand(level, player, 80, 120);
        if (land == null) return false;
        level.setWeatherParameters(0, 12000, true, true);
        MinusZeroGhidorahEntity g = spawn(level, ModEntities.MINUS_ZERO_GHIDORAH.get(), land.getX(), land.getY() + 40, land.getZ(), player, MinusZeroGhidorahEntity.SINGLE);
        if (g == null) return false;
        g.setFlying(true);
        g.setDestination(player.blockPosition());
        // Sometimes Godzilla has already regenerated, and the two are bound to meet.
        if (level.getRandom().nextInt(3) == 0) {
            BlockPos sea = findSea(level, player, 60, 120, 6);
            if (sea != null) {
                MinusOneGodzillaEntity z = spawn(level, ModEntities.MINUS_ONE_GODZILLA.get(), sea.getX(), sea.getY(), sea.getZ(), player, MinusOneGodzillaEntity.MINUS_ZERO);
                if (z != null) z.setDestination(land);
            }
        }
        announce(level, player, "event.reikogodzilla.ghidorah_storm", ChatFormatting.GOLD);
        return true;
    }

    static boolean earthTremors(ServerLevel level, ServerPlayer player) {
        BlockPos land = findLand(level, player, 50, 90);
        if (land == null) return false;
        RandomSource random = level.getRandom();
        if (random.nextInt(6) == 0) {
            GodzillaEarthEntity f = spawn(level, ModEntities.GODZILLA_EARTH.get(), land.getX(), land.getY(), land.getZ(), player, GodzillaEarthEntity.FILIUS);
            if (f != null) f.setDestination(player.blockPosition());
        }
        int n = 4 + random.nextInt(4);
        for (int i = 0; i < n; i++) {
            ServumEntity s = spawn(level, ModEntities.SERVUM.get(), land.getX() + random.nextInt(13) - 6, land.getY() + 6 + random.nextInt(6),
                    land.getZ() + random.nextInt(13) - 6, player, random.nextInt(3) == 0 ? ServumEntity.WORM : ServumEntity.FLYING);
            if (s != null) s.setDestination(player.blockPosition());
        }
        for (ServerPlayer p : level.players()) {
            if (p.distanceToSqr(player) < 128 * 128) p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.CONFUSION, 80, 0));
        }
        announce(level, player, "event.reikogodzilla.earth_tremors", ChatFormatting.DARK_GREEN);
        return true;
    }

    static boolean catastrophe(ServerLevel level, ServerPlayer player) {
        BlockPos sea = findSea(level, player, 50, 100, 4);
        if (sea == null) return false;
        BlockPos land = findLand(level, player, 20, 80);
        if (land == null) land = player.blockPosition();
        SingularGodzillaEntity g = spawn(level, ModEntities.SINGULAR_GODZILLA.get(), sea.getX(), sea.getY(), sea.getZ(), player, SingularGodzillaEntity.AQUATILIS);
        if (g != null) g.setDestination(player.blockPosition());
        RedDustBlock.shed(level, land, 10, 60);
        RandomSource random = level.getRandom();
        for (int i = 0; i < 7; i++) {
            SingularRodanEntity r = spawn(level, ModEntities.SINGULAR_RODAN.get(), land.getX() + random.nextInt(9) - 4, land.getY() + 12, land.getZ() + random.nextInt(9) - 4,
                    player, i == 0 ? SingularRodanEntity.ADULT : SingularRodanEntity.JUVENILE);
            if (r != null) r.setFlying(true);
        }
        announce(level, player, "event.reikogodzilla.catastrophe", ChatFormatting.RED);
        return true;
    }

    static boolean titanX(ServerLevel level, ServerPlayer player) {
        BlockPos sea = findSea(level, player, 50, 110, 8);
        if (sea == null) return false;
        TitanXEntity t = spawn(level, ModEntities.TITAN_X.get(), sea.getX(), sea.getY(), sea.getZ(), player, TitanXEntity.WILD);
        if (t == null) return false;
        level.sendParticles(ParticleTypes.GLOW, sea.getX(), level.getSeaLevel(), sea.getZ(), 200, 10, 1, 10, 0.05);
        announce(level, player, "event.reikogodzilla.titan_x", ChatFormatting.AQUA);
        return true;
    }

    static boolean giganInvasion(ServerLevel level, ServerPlayer player) {
        BlockPos land = findLand(level, player, 60, 100);
        if (land == null) return false;
        RandomSource random = level.getRandom();
        GiganEntity rex = spawn(level, ModEntities.GIGAN.get(), land.getX(), land.getY() + 40, land.getZ(), player, GiganEntity.REX);
        if (rex == null) return false;
        rex.setFlying(true);
        rex.setDestination(player.blockPosition());
        for (int i = 0; i < 4; i++) {
            GiganEntity miles = spawn(level, ModEntities.GIGAN.get(), land.getX() + random.nextInt(31) - 15, land.getY() + 35, land.getZ() + random.nextInt(31) - 15,
                    player, GiganEntity.MILES);
            if (miles != null) {
                miles.setFlying(true);
                miles.setDestination(player.blockPosition());
            }
        }
        announce(level, player, "event.reikogodzilla.gigan_invasion", ChatFormatting.DARK_RED);
        return true;
    }

    static boolean planetEater(ServerLevel level, ServerPlayer player) {
        BlockPos land = findLand(level, player, 30, 50);
        if (land == null) return false;
        level.setBlock(land.below(), net.minecraft.world.level.block.Blocks.QUARTZ_BRICKS.defaultBlockState(), 3);
        level.setBlock(land, ModBlocks.GARBETRIUM_TOTEM.get().defaultBlockState(), 3);
        summonPlanetEater(level, land, player);
        return true;
    }

    /** Opens rifts in the sky over a Garbetrium totem; three of Ghidorah's necks pour through. */
    public static void summonPlanetEater(ServerLevel level, BlockPos totem, ServerPlayer player) {
        RandomSource random = level.getRandom();
        for (int i = 0; i < 3; i++) {
            double a = Math.PI * 2 * i / 3;
            double x = totem.getX() + Math.cos(a) * 18, z = totem.getZ() + Math.sin(a) * 18, y = totem.getY() + 36 + random.nextInt(8);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y, z, 200, 3, 3, 3, 0.5);
            level.sendParticles(ParticleTypes.END_ROD, x, y, z, 60, 2, 2, 2, 0.1);
            PlanetEaterGhidorahEntity neck = spawn(level, ModEntities.PLANET_EATER_GHIDORAH.get(), x, y, z, player, -1);
            if (neck != null) {
                neck.setObserver(totem);
                neck.setFlying(true);
                neck.setDestination(totem);
            }
        }
        level.playSound(null, totem, net.minecraft.sounds.SoundEvents.END_PORTAL_SPAWN, net.minecraft.sounds.SoundSource.HOSTILE, 8f, 0.5f);
        announce(level, player, "event.reikogodzilla.planet_eater", ChatFormatting.GOLD);
    }

    static boolean monarch(ServerLevel level, ServerPlayer player) {
        boolean cold = level.getBiome(player.blockPosition()).value().coldEnoughToSnow(player.blockPosition());
        BlockPos land = findLand(level, player, 50, 90);
        if (land == null) return false;
        KaijuEntity titan;
        if (cold) {
            ClassicEvents.eruption(level, land);
            titan = spawn(level, ModEntities.FROST_VARK.get(), land.getX(), land.getY(), land.getZ(), player, -1);
        } else {
            titan = spawn(level, ModEntities.ION_DRAGON.get(), land.getX(), land.getY() + 30, land.getZ(), player, -1);
            if (titan instanceof FlyingKaijuEntity f) f.setFlying(true);
        }
        if (titan == null) return false;
        titan.setDestination(player.blockPosition());
        announce(level, player, cold ? "event.reikogodzilla.frost_vark" : "event.reikogodzilla.ion_dragon", ChatFormatting.YELLOW);
        return true;
    }

    static boolean kraken(ServerLevel level, ServerPlayer player) {
        BlockPos sea = findSea(level, player, 30, 70, 6);
        if (sea == null) return false;
        KrakenEntity k = spawn(level, ModEntities.KRAKEN.get(), sea.getX(), sea.getY(), sea.getZ(), player, -1);
        if (k == null) return false;
        announce(level, player, "event.reikogodzilla.kraken", ChatFormatting.DARK_PURPLE);
        return true;
    }

    static boolean supernova(ServerLevel level, ServerPlayer player) {
        if (!GodzillaConfig.get(GodzillaConfig.SUPERNOVA_PLACEHOLDER, false)) return false;
        BlockPos land = findLand(level, player, 70, 110);
        if (land == null) return false;
        KaijuEntity s = spawn(level, ModEntities.SPACE_GODZILLA.get(), land.getX(), land.getY() + 40, land.getZ(), player,
                com.reiko45.godzilla.entity.kaiju.SpaceGodzillaEntity.MONSTERVERSE);
        if (s == null) return false;
        s.setDestination(player.blockPosition());
        announce(level, player, "event.reikogodzilla.supernova", ChatFormatting.LIGHT_PURPLE);
        return true;
    }

    static int surface(ServerLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
    }
}

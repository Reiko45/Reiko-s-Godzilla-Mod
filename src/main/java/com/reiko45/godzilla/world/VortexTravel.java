package com.reiko45.godzilla.world;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.ReikoGodzilla;
import com.reiko45.godzilla.entity.monsterverse.HeavEntity;
import com.reiko45.godzilla.registry.ModBlocks;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Travel through Hollow Earth vortices. Overworld vortices lead into the Hollow Earth at the same coordinates (a
 * return vortex is opened next to where you land); vortices in the Hollow Earth lead back up to the surface.
 */
public final class VortexTravel {
    public static final ResourceKey<Level> HOLLOW_EARTH = ResourceKey.create(Registries.DIMENSION, ReikoGodzilla.id("hollow_earth"));

    private VortexTravel() {}

    public static void enter(ServerLevel level, Entity entity) {
        Entity root = entity.getRootVehicle();
        if (root.isOnPortalCooldown() || root.isRemoved()) return;
        boolean carriesPlayer = root.getPassengersAndSelf().anyMatch(e -> e instanceof ServerPlayer);
        if (!carriesPlayer) return;
        travel(level, root);
    }

    public static void travel(ServerLevel from, Entity root) {
        boolean toHollow = from.dimension() != HOLLOW_EARTH;
        ServerLevel to = from.getServer().getLevel(toHollow ? HOLLOW_EARTH : Level.OVERWORLD);
        if (to == null) return;
        boolean shielded = root instanceof HeavEntity;
        List<ServerPlayer> players = new ArrayList<>();
        if (root instanceof ServerPlayer p) players.add(p);
        root.getIndirectPassengers().forEach(e -> {
            if (e instanceof ServerPlayer p) players.add(p);
        });
        BlockPos source = root.blockPosition();
        BlockPos dest = toHollow ? findHollowLanding(to, source.offset(6, 0, 0)) : findSurface(to, source.offset(8, 0, 0));
        Vec3 at = Vec3.atBottomCenterOf(dest).add(0, shielded ? 1.5 : 0, 0);
        for (ServerPlayer p : players) {
            from.playSound(null, p.getX(), p.getY(), p.getZ(), ModSounds.VORTEX_WHOOSH.get(), SoundSource.PLAYERS, 2f, 0.7f);
        }
        Entity moved = root.changeDimension(new DimensionTransition(to, at, Vec3.ZERO, root.getYRot(), root.getXRot(), DimensionTransition.PLACE_PORTAL_TICKET));
        if (moved == null) return;
        moved.setPortalCooldown(300);
        moved.getIndirectPassengers().forEach(e -> e.setPortalCooldown(300));
        if (toHollow) ensureReturnVortex(to, dest);
        for (ServerPlayer p : players) {
            p.setPortalCooldown(300);
            to.playSound(null, at.x, at.y, at.z, ModSounds.VORTEX_WHOOSH.get(), SoundSource.PLAYERS, 2f, 1.0f);
            if (shielded) {
                p.displayClientMessage(Component.translatable(toHollow ? "message.reikogodzilla.vortex_heav_in" : "message.reikogodzilla.vortex_heav_out")
                        .withStyle(ChatFormatting.AQUA), true);
            } else if (!p.isCreative() && !p.isSpectator()) {
                // The gravity flip without reverse-gravity drives.
                p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
                p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
                p.hurt(ModDamageTypes.source(to, ModDamageTypes.GRAVITY_INVERSION, null), (float) GodzillaConfig.get(GodzillaConfig.VORTEX_DAMAGE, 6.0));
                p.displayClientMessage(Component.translatable("message.reikogodzilla.vortex_inversion").withStyle(ChatFormatting.RED), true);
            }
        }
    }

    private static BlockPos findSurface(ServerLevel level, BlockPos near) {
        level.getChunk(near.getX() >> 4, near.getZ() >> 4);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, near.getX(), near.getZ());
        return new BlockPos(near.getX(), Math.max(y, level.getMinBuildHeight() + 1), near.getZ());
    }

    /** A floor with headroom inside the Hollow Earth's caverns, or a platform carved out if there isn't one. */
    static BlockPos findHollowLanding(ServerLevel level, BlockPos near) {
        int top = Math.min(level.getMaxBuildHeight() - 6, 110);
        for (int ring = 0; ring <= 24; ring += 4) {
            for (int dx = -ring; dx <= ring; dx += 4) {
                for (int dz = -ring; dz <= ring; dz += 4) {
                    if (ring > 0 && Math.abs(dx) != ring && Math.abs(dz) != ring) continue;
                    int x = near.getX() + dx, z = near.getZ() + dz;
                    level.getChunk(x >> 4, z >> 4);
                    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, top, z);
                    for (int y = top; y > 12; y--) {
                        pos.setY(y);
                        BlockState floor = level.getBlockState(pos.below());
                        if (floor.isSolidRender(level, pos.below()) && level.getBlockState(pos).isAir()
                                && level.getBlockState(pos.above()).isAir() && level.getBlockState(pos.above(2)).isAir()
                                && level.getBlockState(pos.above(3)).isAir()) {
                            return pos.immutable();
                        }
                    }
                }
            }
        }
        BlockPos platform = new BlockPos(near.getX(), 64, near.getZ());
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                level.setBlockAndUpdate(platform.offset(dx, -1, dz), Blocks.MOSSY_COBBLESTONE.defaultBlockState());
                for (int dy = 0; dy < 5; dy++) level.setBlockAndUpdate(platform.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
            }
        }
        return platform;
    }

    /** A way home: a small vortex pad beside the landing spot, unless one is already close. */
    private static void ensureReturnVortex(ServerLevel level, BlockPos landing) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -24; dx <= 24; dx++) {
            for (int dz = -24; dz <= 24; dz++) {
                for (int dy = -8; dy <= 8; dy++) {
                    if (level.getBlockState(pos.set(landing.getX() + dx, landing.getY() + dy, landing.getZ() + dz)).is(ModBlocks.HOLLOW_EARTH_VORTEX.get())) return;
                }
            }
        }
        BlockPos pad = landing.offset(-6, -1, 0);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                boolean rim = Math.abs(dx) == 2 || Math.abs(dz) == 2;
                level.setBlockAndUpdate(pad.offset(dx, -1, dz), Blocks.CRYING_OBSIDIAN.defaultBlockState());
                level.setBlockAndUpdate(pad.offset(dx, 0, dz), rim ? Blocks.AMETHYST_BLOCK.defaultBlockState() : ModBlocks.HOLLOW_EARTH_VORTEX.get().defaultBlockState());
                for (int dy = 1; dy <= 3; dy++) level.setBlockAndUpdate(pad.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
            }
        }
    }
}

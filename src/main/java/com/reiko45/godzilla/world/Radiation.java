package com.reiko45.godzilla.world;

import com.reiko45.godzilla.block.RadiationSourceBlock;
import com.reiko45.godzilla.block.RadiationSourceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/** Finding and draining radiation sources (nuclear warheads, Tiamat's solar crystals). */
public final class Radiation {
    public record Absorbed(int energy, boolean solar) {}

    private Radiation() {}

    /** Nearest loaded radiation source within {@code radius}, or null. Only looks at block entities, so it's cheap. */
    @Nullable
    public static BlockPos findNearest(ServerLevel level, Vec3 center, double radius) {
        int minX = (int) Math.floor((center.x - radius) / 16.0), maxX = (int) Math.floor((center.x + radius) / 16.0);
        int minZ = (int) Math.floor((center.z - radius) / 16.0), maxZ = (int) Math.floor((center.z + radius) / 16.0);
        BlockPos best = null;
        double bestDist = radius * radius;
        for (int cx = minX; cx <= maxX; cx++) {
            for (int cz = minZ; cz <= maxZ; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (!(be instanceof RadiationSourceBlockEntity)) continue;
                    double d = Vec3.atCenterOf(be.getBlockPos()).distanceToSqr(center);
                    if (d < bestDist) {
                        bestDist = d;
                        best = be.getBlockPos();
                    }
                }
            }
        }
        return best;
    }

    /** Drains the source at {@code pos} (it's used up), drawing a stream of energy toward {@code into}. */
    @Nullable
    public static Absorbed drain(ServerLevel level, BlockPos pos, Vec3 into) {
        if (!(level.getBlockState(pos).getBlock() instanceof RadiationSourceBlock source)) return null;
        level.removeBlock(pos, false);
        Vec3 from = Vec3.atCenterOf(pos);
        for (int i = 0; i <= 24; i++) {
            Vec3 p = from.lerp(into, i / 24.0);
            level.sendParticles(source.isSolar() ? ParticleTypes.END_ROD : ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 2, 0.2, 0.2, 0.2, 0.01);
        }
        level.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 3f, 0.5f);
        return new Absorbed(source.energy(), source.isSolar());
    }
}

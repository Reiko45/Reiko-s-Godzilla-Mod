package com.reiko45.godzilla.world;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.registry.ModEffects;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Shimo's ice: freezing terrain, water and lava, and frostbite. */
public final class Frost {
    private Frost() {}

    /** Kaiju fill their freeze meter; everything else gets frostbite. */
    public static void chill(LivingEntity e, float amount) {
        if (e instanceof KaijuEntity k) {
            k.addFreeze(amount);
            return;
        }
        e.clearFire();
        e.addEffect(new MobEffectInstance(ModEffects.FROSTBITE, 200 + (int) (amount * 10), 0));
    }

    /** Where a freezing beam lands: ice and snow instead of a crater. */
    public static void freezeTerrain(ServerLevel level, KaijuEntity kaiju, Vec3 hit, float radius) {
        if (!KaijuDestruction.canGrief(level, kaiju)) {
            level.sendParticles(ParticleTypes.SNOWFLAKE, hit.x, hit.y, hit.z, 30, radius * 0.5, radius * 0.5, radius * 0.5, 0.05);
            return;
        }
        freezeArea(level, BlockPos.containing(hit), Math.max(2, (int) radius), kaiju.getRandom(), true);
        level.sendParticles(ParticleTypes.SNOWFLAKE, hit.x, hit.y, hit.z, 40, radius * 0.5, radius * 0.5, radius * 0.5, 0.05);
    }

    /**
     * Freeze a rough sphere: water to ice, lava to obsidian, snow on top of the ground, and (if {@code chunks}) packed
     * ice growing on surfaces like the ice Shimo's blast leaves behind.
     */
    public static void freezeArea(ServerLevel level, BlockPos center, int radius, RandomSource random, boolean chunks) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int r2 = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz > r2) continue;
                    pos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    if (!level.isLoaded(pos)) continue;
                    BlockState state = level.getBlockState(pos);
                    if (state.getFluidState().is(FluidTags.WATER) && state.getFluidState().isSource()) {
                        level.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
                    } else if (state.getFluidState().is(FluidTags.LAVA)) {
                        level.setBlockAndUpdate(pos, state.getFluidState().isSource() ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState());
                    } else if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) {
                        level.removeBlock(pos, false);
                    } else if (state.isAir() && random.nextInt(3) == 0) {
                        BlockState below = level.getBlockState(pos.below());
                        if (below.isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP)) {
                            BlockState place = chunks && random.nextInt(4) == 0 ? Blocks.PACKED_ICE.defaultBlockState()
                                    : Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1 + random.nextInt(3));
                            level.setBlockAndUpdate(pos, place);
                        }
                    }
                }
            }
        }
    }
}

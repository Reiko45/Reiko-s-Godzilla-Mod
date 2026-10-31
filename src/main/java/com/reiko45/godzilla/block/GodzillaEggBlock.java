package com.reiko45.godzilla.block;

import com.mojang.serialization.MapCodec;
import com.reiko45.godzilla.entity.kaiju.GodzillaJuniorEntity;
import com.reiko45.godzilla.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

/**
 * A Godzillasaurus egg, found in a nest on Monster Island (Godzilla vs. Mechagodzilla II, 1993). Baby Godzilla
 * imprints on the nearest player when it hatches.
 */
public class GodzillaEggBlock extends HatchingEggBlock {
    public static final MapCodec<GodzillaEggBlock> CODEC = codecFor(GodzillaEggBlock::new);

    public GodzillaEggBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void hatch(ServerLevel level, BlockPos pos, RandomSource random) {
        GodzillaJuniorEntity baby = ModEntities.GODZILLA_JUNIOR.get().create(level);
        if (baby == null) return;
        baby.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360f, 0);
        baby.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.SPAWNER, null);
        level.addFreshEntity(baby);
        Player nearest = level.getNearestPlayer(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 32, false);
        if (nearest != null) baby.imprintOn(nearest);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 30, 0.8, 0.8, 0.8, 0.1);
    }
}

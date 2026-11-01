package com.reiko45.godzilla.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stored solar radiation from Tiamat's Arctic lair (Godzilla x Kong: The New Empire). Godzilla took this energy and
 * came out of the ice Evolved.
 */
public class SolarCrystalBlock extends RadiationSourceBlock {
    public static final MapCodec<SolarCrystalBlock> CODEC = simpleCodec(SolarCrystalBlock::new);

    public SolarCrystalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<SolarCrystalBlock> codec() {
        return CODEC;
    }

    @Override
    public int energy() {
        return 100;
    }

    @Override
    public boolean isSolar() {
        return true;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.END_ROD, pos.getX() + random.nextDouble(), pos.getY() + 1.05, pos.getZ() + random.nextDouble(), 0, 0.03, 0);
        }
    }
}

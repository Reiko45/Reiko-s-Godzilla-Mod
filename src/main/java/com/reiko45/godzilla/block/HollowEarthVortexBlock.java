package com.reiko45.godzilla.block;

import com.mojang.serialization.MapCodec;
import com.reiko45.godzilla.world.VortexTravel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A gravity vortex into the Hollow Earth (the Antarctic vortex of Godzilla vs. Kong, the rifts on Skull Island).
 * Anything that falls in is pulled through. Without a HEAV's reverse-gravity drives the inversion hurts.
 */
public class HollowEarthVortexBlock extends Block {
    public static final MapCodec<HollowEarthVortexBlock> CODEC = simpleCodec(HollowEarthVortexBlock::new);

    public HollowEarthVortexBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<HollowEarthVortexBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Block.box(0, 0, 0, 16, 12, 16);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level instanceof ServerLevel server) VortexTravel.enter(server, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        for (int i = 0; i < 3; i++) {
            double a = random.nextDouble() * Math.PI * 2;
            double r = 0.5 + random.nextDouble() * 1.5;
            level.addParticle(ParticleTypes.REVERSE_PORTAL, pos.getX() + 0.5 + Math.cos(a) * r, pos.getY() + 0.8, pos.getZ() + 0.5 + Math.sin(a) * r,
                    -Math.cos(a) * 0.05, 0.15 + random.nextDouble() * 0.2, -Math.sin(a) * 0.05);
        }
        if (random.nextInt(4) == 0) level.addParticle(ParticleTypes.END_ROD, pos.getX() + random.nextDouble(), pos.getY() + 0.9, pos.getZ() + random.nextDouble(), 0, 0.3, 0);
    }
}

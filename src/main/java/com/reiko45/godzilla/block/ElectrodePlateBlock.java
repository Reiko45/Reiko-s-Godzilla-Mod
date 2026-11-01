package com.reiko45.godzilla.block;

import com.mojang.serialization.MapCodec;
import com.reiko45.godzilla.entity.showa.HedorahEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The Defense Force's giant electrode plates (Godzilla vs. Hedorah, 1971). Power one with redstone and it arcs
 * electricity at any Hedorah within 24 blocks, drying it out: no healing, double damage, and it slowly crumbles.
 */
public class ElectrodePlateBlock extends Block {
    public static final MapCodec<ElectrodePlateBlock> CODEC = simpleCodec(ElectrodePlateBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final double RANGE = 24;

    public ElectrodePlateBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected MapCodec<ElectrodePlateBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos from, boolean moving) {
        boolean powered = level.hasNeighborSignal(pos);
        if (powered == state.getValue(POWERED)) return;
        level.setBlock(pos, state.setValue(POWERED, powered), 3);
        if (powered) level.scheduleTick(pos, this, 10);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(POWERED)) return;
        Vec3 c = Vec3.atCenterOf(pos);
        for (HedorahEntity hedorah : level.getEntitiesOfClass(HedorahEntity.class, new AABB(pos).inflate(RANGE))) {
            hedorah.applyDesiccation(60);
            Vec3 to = hedorah.getBoundingBox().getCenter();
            for (int i = 0; i <= 16; i++) {
                Vec3 p = c.lerp(to, i / 16.0).add((random.nextDouble() - 0.5) * 0.8, (random.nextDouble() - 0.5) * 0.8, (random.nextDouble() - 0.5) * 0.8);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 2, 0.05, 0.05, 0.05, 0.05);
            }
            level.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.BLOCKS, 1.5f, 1.6f);
        }
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, c.x, c.y + 0.6, c.z, 6, 0.3, 0.3, 0.3, 0.1);
        level.scheduleTick(pos, this, 20);
    }
}

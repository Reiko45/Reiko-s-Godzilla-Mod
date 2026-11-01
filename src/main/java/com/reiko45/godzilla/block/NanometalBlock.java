package com.reiko45.godzilla.block;

import com.mojang.serialization.MapCodec;
import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.KaijuEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Nanometal (the anime trilogy): the self-replicating metal that grew from Mechagodzilla's head into a whole city.
 * While a Mechagodzilla City core is alive nearby it is ACTIVE and slowly absorbs the stone and metal around it; when
 * the core is destroyed it goes inert.
 */
public class NanometalBlock extends Block {
    public static final MapCodec<NanometalBlock> CODEC = simpleCodec(NanometalBlock::new);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public NanometalBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected MapCodec<NanometalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(ACTIVE);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int radius = GodzillaConfig.get(GodzillaConfig.NANOMETAL_RADIUS, 48);
        if (!com.reiko45.godzilla.world.NanometalCores.isNear(level, pos, radius)) {
            level.setBlock(pos, state.setValue(ACTIVE, false), 3);
            return;
        }
        Direction dir = Direction.getRandom(random);
        BlockPos to = pos.relative(dir);
        if (com.reiko45.godzilla.world.NanometalCores.isNear(level, to, radius) && absorbs(level.getBlockState(to))) {
            level.setBlock(to, state, 3);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, to.getX() + 0.5, to.getY() + 0.5, to.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.05);
        }
    }

    public static boolean absorbs(BlockState state) {
        return state.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD) || state.is(Blocks.IRON_BLOCK) || state.is(Blocks.COBBLESTONE)
                || state.is(Blocks.STONE_BRICKS) || state.is(Blocks.SMOOTH_STONE) || state.is(Blocks.GRAVEL) || state.is(Blocks.IRON_BARS);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(ACTIVE) && random.nextInt(10) == 0) {
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, pos.getX() + random.nextDouble(), pos.getY() + 1.05, pos.getZ() + random.nextDouble(), 0, 0.02, 0);
        }
    }
}

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
 * Red Dust (Godzilla Singular Point): the Archetype-laden powder that Godzilla and the other Singular Point kaiju
 * shed. It creeps outward a few generations from where it fell, and breathing it in weakens people. An Orthogonal
 * Diagonalizer crystallizes it.
 */
public class RedDustBlock extends Block {
    public static final MapCodec<RedDustBlock> CODEC = simpleCodec(RedDustBlock::new);
    public static final IntegerProperty GEN = IntegerProperty.create("gen", 0, 4);
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 2, 16);

    public RedDustBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(GEN, 0));
    }

    @Override
    protected MapCodec<RedDustBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(GEN);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.isFaceFull(level.getBlockState(pos.below()).getCollisionShape(level, pos.below()), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return dir == Direction.DOWN && !canSurvive(state, level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, dir, neighbor, level, pos, neighborPos);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(GEN) < 4;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!GodzillaConfig.get(GodzillaConfig.RED_DUST_SPREADS, true)) return;
        BlockPos to = pos.offset(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
        if (to.equals(pos) || !level.getBlockState(to).isAir()) return;
        BlockState next = state.setValue(GEN, state.getValue(GEN) + 1);
        if (next.canSurvive(level, to)) level.setBlock(to, next, 3);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && entity instanceof Player p && !p.isCreative() && level.getGameTime() % 40 == 0) {
            p.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 0));
            p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(6) == 0) {
            level.addParticle(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.85f, 0.15f, 0.1f), 1.0f),
                    pos.getX() + random.nextDouble(), pos.getY() + 0.3 + random.nextDouble() * 0.6, pos.getZ() + random.nextDouble(), 0, 0.02, 0);
        }
    }

    /** Sheds a patch of fresh dust around {@code center} (a kaiju walking through). */
    public static int shed(ServerLevel level, BlockPos center, int radius, int count) {
        RandomSource random = level.getRandom();
        BlockState dust = com.reiko45.godzilla.registry.ModBlocks.RED_DUST.get().defaultBlockState();
        int placed = 0;
        for (int i = 0; i < count; i++) {
            int x = center.getX() + random.nextInt(radius * 2 + 1) - radius, z = center.getZ() + random.nextInt(radius * 2 + 1) - radius;
            if (!level.hasChunkAt(new BlockPos(x, 0, z))) continue;
            BlockPos top = new BlockPos(x, level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
            if (level.getBlockState(top).isAir() && dust.canSurvive(level, top)) {
                level.setBlock(top, dust, 3);
                placed++;
            }
        }
        return placed;
    }

    /** Counts dust blocks around a point (sampled, so it stays cheap). */
    public static int density(ServerLevel level, BlockPos center, int radius) {
        RandomSource random = level.getRandom();
        int found = 0;
        for (int i = 0; i < 48; i++) {
            int x = center.getX() + random.nextInt(radius * 2 + 1) - radius, z = center.getZ() + random.nextInt(radius * 2 + 1) - radius;
            if (!level.hasChunkAt(new BlockPos(x, 0, z))) continue;
            int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (level.getBlockState(new BlockPos(x, y, z)).getBlock() instanceof RedDustBlock || level.getBlockState(new BlockPos(x, y - 1, z)).getBlock() instanceof RedDustBlock) found++;
        }
        return found;
    }

    /** The Orthogonal Diagonalizer: every bit of dust nearby turns into inert Archetype crystal. */
    public static int crystallize(ServerLevel level, BlockPos center, int radius) {
        int done = 0;
        BlockState crystal = com.reiko45.godzilla.registry.ModBlocks.ARCHETYPE_CRYSTAL.get().defaultBlockState();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -8, -radius), center.offset(radius, 8, radius))) {
            if (level.getBlockState(pos).getBlock() instanceof RedDustBlock) {
                level.setBlock(pos, done % 5 == 0 ? crystal : Blocks.AIR.defaultBlockState(), 3);
                if (done % 6 == 0) level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2, 0.3, 0.3, 0.3, 0.02);
                done++;
            }
        }
        return done;
    }
}

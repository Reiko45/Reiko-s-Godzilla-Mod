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
 * Titan X's egg (Monarch: Legacy of Monsters, season 2). Apex stole it and drove the mother mad. Take it from its
 * nest and any Titan X nearby comes after you; bring it back to the water beside her and she calms for good.
 */
public class TitanXEggBlock extends Block {
    public static final MapCodec<TitanXEggBlock> CODEC = simpleCodec(TitanXEggBlock::new);
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 15, 14);

    public TitanXEggBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<TitanXEggBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level instanceof ServerLevel server && !player.isCreative()) {
            for (var titan : server.getEntitiesOfClass(com.reiko45.godzilla.entity.reiwa.TitanXEntity.class, new AABB(pos).inflate(160))) titan.eggTaken(player);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @javax.annotation.Nullable LivingEntity placer, net.minecraft.world.item.ItemStack stack) {
        if (!(level instanceof ServerLevel server)) return;
        boolean wet = false;
        for (Direction d : Direction.values()) wet |= level.getFluidState(pos.relative(d)).is(net.minecraft.tags.FluidTags.WATER);
        if (!wet) return;
        for (var titan : server.getEntitiesOfClass(com.reiko45.godzilla.entity.reiwa.TitanXEntity.class, new AABB(pos).inflate(48))) {
            titan.eggReturned(placer instanceof Player p ? p : null);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) == 0) level.addParticle(ParticleTypes.GLOW, pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6, pos.getY() + 0.6,
                pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6, 0, 0.02, 0);
    }
}

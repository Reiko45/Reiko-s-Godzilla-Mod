package com.reiko45.godzilla.block;

import com.mojang.serialization.MapCodec;
import com.reiko45.godzilla.entity.showa.KamacurasEntity;
import com.reiko45.godzilla.entity.showa.MinillaEntity;
import com.reiko45.godzilla.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Minilla's egg (Son of Godzilla, 1967), buried on Sollgel Island and kept warm by the heat of the earth. It only
 * hatches with heat beneath it: lava, magma, fire or a campfire within two blocks below. The hatchling attaches itself
 * to the nearest player. Its hatching call carries a long way, and the Kamacuras come to see what it is.
 */
public class MinillaEggBlock extends HatchingEggBlock {
    public static final MapCodec<MinillaEggBlock> CODEC = codecFor(MinillaEggBlock::new);

    public MinillaEggBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    private static boolean isWarm(ServerLevel level, BlockPos pos) {
        for (int dy = 1; dy <= 2; dy++) {
            BlockState below = level.getBlockState(pos.below(dy));
            if (below.is(Blocks.LAVA) || below.is(Blocks.MAGMA_BLOCK) || below.is(BlockTags.FIRE) || below.is(BlockTags.CAMPFIRES)) return true;
        }
        return false;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!isWarm(level, pos)) return;
        super.randomTick(state, level, pos, random);
    }

    @Override
    protected void hatch(ServerLevel level, BlockPos pos, RandomSource random) {
        MinillaEntity minilla = ModEntities.MINILLA.get().create(level);
        if (minilla == null) return;
        minilla.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360f, 0);
        minilla.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.BREEDING, null);
        Player nearest = level.getNearestPlayer(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 16, false);
        if (nearest != null) minilla.setOwner(nearest);
        level.addFreshEntity(minilla);
        for (Player p : level.players()) {
            if (p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 128 * 128) {
                p.sendSystemMessage(Component.translatable("message.reikogodzilla.minilla_hatched").withStyle(ChatFormatting.GREEN));
            }
        }
        // The hatching call draws the Kamacuras.
        int count = 2 + random.nextInt(2);
        for (int i = 0; i < count; i++) {
            KamacurasEntity mantis = ModEntities.KAMACURAS.get().create(level);
            if (mantis == null) continue;
            double a = random.nextDouble() * Math.PI * 2;
            int x = pos.getX() + (int) (Math.cos(a) * 40), z = pos.getZ() + (int) (Math.sin(a) * 40);
            if (!level.hasChunkAt(new BlockPos(x, 0, z))) continue;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            mantis.moveTo(x + 0.5, y + 6, z + 0.5, random.nextFloat() * 360f, 0);
            mantis.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
            mantis.setDestination(pos);
            level.addFreshEntity(mantis);
        }
    }
}

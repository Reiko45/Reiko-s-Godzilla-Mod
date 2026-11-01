package com.reiko45.godzilla.block;

import com.mojang.serialization.MapCodec;
import com.reiko45.godzilla.entity.monsterverse.MutoEntity;
import com.reiko45.godzilla.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Block;

/**
 * A MUTO egg from the female's nest (Godzilla, 2014). Like all kaiju eggs it cracks while someone is close by; this
 * time you want to smash it before it hatches.
 */
public class MutoEggBlock extends HatchingEggBlock {
    public static final MapCodec<MutoEggBlock> CODEC = codecFor(MutoEggBlock::new);

    public MutoEggBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void hatch(ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) != 0) return;
        MutoEntity muto = ModEntities.MUTO.get().create(level);
        if (muto == null) return;
        muto.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360f, 0);
        muto.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
        muto.setVariant(MutoEntity.MALE);
        muto.setHealth(muto.getMaxHealth() * 0.5f);
        level.addFreshEntity(muto);
        for (var p : level.players()) {
            if (p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 128 * 128) {
                p.sendSystemMessage(Component.translatable("message.reikogodzilla.muto_hatched").withStyle(ChatFormatting.DARK_GREEN));
            }
        }
    }
}

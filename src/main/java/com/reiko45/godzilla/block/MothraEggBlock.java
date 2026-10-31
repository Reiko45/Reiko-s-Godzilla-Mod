package com.reiko45.godzilla.block;

import com.mojang.serialization.MapCodec;
import com.reiko45.godzilla.entity.kaiju.MothraLarvaEntity;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Block;

/** Mothra's giant egg from Infant Island. Hatches a Mothra larva; sometimes twins, as in Mothra vs. Godzilla (1964). */
public class MothraEggBlock extends HatchingEggBlock {
    public static final MapCodec<MothraEggBlock> CODEC = codecFor(MothraEggBlock::new);

    public MothraEggBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void hatch(ServerLevel level, BlockPos pos, RandomSource random) {
        level.playSound(null, pos, ModSounds.MOTHRA_CRY.get(), SoundSource.NEUTRAL, 6f, 1.3f);
        int count = random.nextInt(4) == 0 ? 2 : 1;
        for (int i = 0; i < count; i++) {
            MothraLarvaEntity larva = ModEntities.MOTHRA_LARVA.get().create(level);
            if (larva == null) continue;
            larva.moveTo(pos.getX() + 0.5 + i * 3, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360f, 0);
            larva.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.SPAWNER, null);
            level.addFreshEntity(larva);
        }
        level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 60, 1, 1, 1, 0.1);
        for (var player : level.players()) {
            if (player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 64 * 64) {
                player.displayClientMessage(Component.translatable(count == 2 ? "message.reikogodzilla.mothra_twins" : "message.reikogodzilla.mothra_hatched"), true);
            }
        }
    }
}

package com.reiko45.godzilla.block;

import com.reiko45.godzilla.entity.showa.KingCaesarEntity;
import com.reiko45.godzilla.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class ShisaStatueBlockEntity extends BlockEntity {
    public ShisaStatueBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SHISA_STATUE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ShisaStatueBlockEntity be) {
        if (level.getGameTime() % 40 != 0 || !(level instanceof ServerLevel server)) return;
        if (!songPlaying(server, pos)) return;
        server.sendParticles(ParticleTypes.NOTE, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 3, 0.3, 0.2, 0.3, 1);
        for (KingCaesarEntity caesar : server.getEntitiesOfClass(KingCaesarEntity.class, new AABB(pos).inflate(64), KingCaesarEntity::isDormant)) {
            caesar.wakeUp();
        }
    }

    private static boolean songPlaying(ServerLevel level, BlockPos pos) {
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-6, -3, -6), pos.offset(6, 3, 6))) {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof JukeboxBlock && s.getValue(JukeboxBlock.HAS_RECORD)) return true;
        }
        return false;
    }
}

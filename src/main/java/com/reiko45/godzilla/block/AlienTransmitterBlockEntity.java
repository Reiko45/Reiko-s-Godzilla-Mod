package com.reiko45.godzilla.block;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.registry.ModBlockEntities;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.world.AlienControl;
import com.reiko45.godzilla.world.AlienSignal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class AlienTransmitterBlockEntity extends BlockEntity {
    public AlienTransmitterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALIEN_TRANSMITTER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AlienTransmitterBlockEntity be) {
        if (level.getGameTime() % 20 != 0 || !(level instanceof ServerLevel server)) return;
        AlienSignal signal = state.getValue(AlienTransmitterBlock.SIGNAL);
        Vec3 c = Vec3.atCenterOf(pos);
        int controlled = AlienControl.broadcast(server, c, GodzillaConfig.get(GodzillaConfig.CONTROL_RANGE, 160), signal);
        server.sendParticles(new DustParticleOptions(signal.color(), 1.5f), c.x, c.y + 0.8, c.z, 6, 0.2, 0.6, 0.2, 0);
        if (controlled > 0 && level.getGameTime() % 100 == 0) {
            server.playSound(null, pos, ModSounds.ALIEN_SIGNAL.get(), SoundSource.BLOCKS, 2f, 0.8f);
        }
    }
}

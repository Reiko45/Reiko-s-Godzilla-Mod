package com.reiko45.godzilla.block;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.registry.ModBlockEntities;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Broadcasts the ORCA's signal every two seconds while it's on. */
public class OrcaBlockEntity extends BlockEntity {
    public OrcaBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ORCA.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, OrcaBlockEntity orca) {
        if (!(level instanceof ServerLevel server) || level.getGameTime() % 40 != 0 || !OrcaBlock.isBroadcasting(state)) return;
        double range = GodzillaConfig.get(GodzillaConfig.ORCA_RANGE, 96);
        OrcaBlock.Mode mode = state.getValue(OrcaBlock.MODE);
        server.sendParticles(ParticleTypes.NOTE, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 3, 0.4, 0.2, 0.4, 1);
        server.sendParticles(ParticleTypes.SONIC_BOOM, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 1, 0, 0, 0, 0);
        if (level.getGameTime() % 120 == 0) {
            server.playSound(null, pos, ModSounds.ORCA_SIGNAL.get(), SoundSource.BLOCKS, 3f, mode == OrcaBlock.Mode.ALPHA ? 0.5f : mode == OrcaBlock.Mode.LURE ? 1.0f : 1.5f);
        }
        AABB area = new AABB(pos).inflate(mode == OrcaBlock.Mode.ALPHA ? range * 2.5 : range);
        for (KaijuEntity k : server.getEntitiesOfClass(KaijuEntity.class, area, k -> k.isAlive() && k.getFaction() != KaijuFaction.HUMAN)) {
            switch (mode) {
                case CALM -> k.calm(100);
                case LURE -> {
                    if (k.getTarget() == null && !k.ignoresOrca()) k.setDestination(pos);
                }
                case ALPHA -> {
                    k.wakeUp();
                    // A false Alpha signal riles up every Titan close to it.
                    if (!k.ignoresOrca() && k.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < range * range && k.getTarget() == null) {
                        Player nearest = server.getNearestPlayer(k, range);
                        if (nearest != null && !nearest.isCreative() && !nearest.isSpectator()) k.setTarget(nearest);
                        else k.setDestination(pos);
                    }
                }
            }
        }
    }
}

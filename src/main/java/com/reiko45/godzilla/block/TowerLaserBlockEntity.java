package com.reiko45.godzilla.block;

import com.reiko45.godzilla.entity.projectile.KaijuMissileEntity;
import com.reiko45.godzilla.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class TowerLaserBlockEntity extends BlockEntity {
    private static final double RANGE = 48;

    public TowerLaserBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOWER_LASER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TowerLaserBlockEntity be) {
        if (level.getGameTime() % 50 != 0 || !(level instanceof ServerLevel server)) return;
        Vec3 from = Vec3.atCenterOf(pos);
        Player target = server.getNearestPlayer(from.x, from.y, from.z, RANGE, p -> !((Player) p).isCreative() && !p.isSpectator());
        if (target == null) return;
        Vec3 to = target.getEyePosition();
        Vec3 dir = to.subtract(from).normalize();
        Vec3 start = from.add(dir.scale(1.0));
        if (server.clip(new ClipContext(start, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, target)).getType() != HitResult.Type.MISS) return;
        KaijuMissileEntity bolt = new KaijuMissileEntity(server, start.x, start.y, start.z, KaijuMissileEntity.Payload.LASER);
        bolt.shoot(dir.x, dir.y, dir.z, 2.0f, 1.0f);
        server.addFreshEntity(bolt);
        server.playSound(null, pos, SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 3f, 1.4f);
    }
}

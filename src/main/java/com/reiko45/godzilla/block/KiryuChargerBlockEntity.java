package com.reiko45.godzilla.block;

import com.reiko45.godzilla.entity.millennium.KiryuEntity;
import com.reiko45.godzilla.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class KiryuChargerBlockEntity extends BlockEntity {
    public KiryuChargerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.KIRYU_CHARGER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, KiryuChargerBlockEntity be) {
        if (level.getGameTime() % 20 != 0 || !(level instanceof ServerLevel server)) return;
        Vec3 c = Vec3.atCenterOf(pos);
        for (KiryuEntity kiryu : server.getEntitiesOfClass(KiryuEntity.class, new AABB(pos).inflate(10), k -> !k.isRampaging())) {
            if (kiryu.getEnergy() >= 100f) continue;
            kiryu.addEnergy(2f);
            Vec3 to = kiryu.getBoundingBox().getCenter();
            for (int i = 0; i <= 8; i++) {
                Vec3 p = c.lerp(to, i / 8.0);
                server.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0);
            }
        }
    }
}

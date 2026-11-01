package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/** Megalon traps his enemy inside a ring of fire, then torments it (Godzilla vs. Megalon). */
public class FireRingAbility extends KaijuAbility {
    @Nullable
    private Vec3 center;
    private double radius;

    public FireRingAbility() {
        super("fire_ring", 20, 100, 500, "charge", "beam");
        this.minRange = 6;
        this.maxRange = 40;
        this.useChance = 0.02f;
    }

    @Override
    public void activate(KaijuEntity kaiju, @Nullable LivingEntity target) {
        if (target == null) {
            kaiju.requestAbilityEnd();
            return;
        }
        center = target.position();
        radius = Math.max(5, target.getBbWidth() * 1.5 + 3);
        if (!(kaiju.level() instanceof ServerLevel level)) return;
        level.playSound(null, center.x, center.y, center.z, SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 5f, 0.5f);
        boolean fire = KaijuDestruction.canGrief(level, kaiju) && GodzillaConfig.get(GodzillaConfig.BEAM_FIRE, true);
        if (!fire) return;
        int points = (int) (radius * 6);
        for (int i = 0; i < points; i++) {
            double a = Math.PI * 2 * i / points;
            int x = (int) Math.floor(center.x + Math.cos(a) * radius), z = (int) Math.floor(center.z + Math.sin(a) * radius);
            BlockPos top = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
            if (level.getBlockState(top).isAir() && level.getFluidState(top.below()).isEmpty()) {
                level.setBlockAndUpdate(top, BaseFireBlock.getState(level, top));
            }
        }
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (center == null || !(kaiju.level() instanceof ServerLevel level)) return;
        if (tick % 3 == 0) {
            for (int i = 0; i < 24; i++) {
                double a = kaiju.getRandom().nextDouble() * Math.PI * 2;
                level.sendParticles(ParticleTypes.FLAME, center.x + Math.cos(a) * radius, center.y + kaiju.getRandom().nextDouble() * 3, center.z + Math.sin(a) * radius,
                        1, 0, 0.05, 0, 0.01);
            }
        }
        if (tick % 10 != 0) return;
        // Anything trying to cross the ring is burned and thrown back inside.
        for (LivingEntity e : AreaAbility.victims(kaiju, 64)) {
            double d = Math.sqrt(e.distanceToSqr(center.x, e.getY(), center.z));
            if (Math.abs(d - radius) < 1.5 + e.getBbWidth() * 0.5) {
                e.igniteForSeconds(5);
                e.hurt(kaiju.damageSources().onFire(), 4f);
                Vec3 in = center.subtract(e.position()).multiply(1, 0, 1).normalize();
                e.push(in.x * 0.6, 0.2, in.z * 0.6);
                e.hurtMarked = true;
            }
        }
    }

    @Override
    public void stop(KaijuEntity kaiju) {
        center = null;
    }
}

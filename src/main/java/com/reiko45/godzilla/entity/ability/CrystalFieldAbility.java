package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.kaiju.SpaceCrystalEntity;
import com.reiko45.godzilla.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.Heightmap;

import javax.annotation.Nullable;

/** SpaceGodzilla raises giant crystals around itself. While they stand, they feed it power. */
public class CrystalFieldAbility extends KaijuAbility {
    public CrystalFieldAbility() {
        super("crystal_field", 30, 20, 900, "charge", "roar");
        this.minRange = 0;
        this.maxRange = 64;
        this.useChance = 0.02f;
    }

    @Override
    protected boolean extraStartCheck(KaijuEntity kaiju, LivingEntity target) {
        return kaiju.level().getEntitiesOfClass(SpaceCrystalEntity.class, kaiju.getBoundingBox().inflate(64)).size() < 2;
    }

    @Override
    public void activate(KaijuEntity kaiju, @Nullable LivingEntity target) {
        if (!(kaiju.level() instanceof ServerLevel level)) return;
        level.playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), SoundEvents.AMETHYST_CLUSTER_PLACE, SoundSource.HOSTILE, 8f, 0.4f);
        int count = 4 + kaiju.getRandom().nextInt(3);
        double base = kaiju.getRandom().nextDouble() * Math.PI * 2;
        for (int i = 0; i < count; i++) {
            double a = base + i * Math.PI * 2 / count;
            double d = (14 + kaiju.getRandom().nextDouble() * 10) * Math.max(1.0, Math.sqrt(kaiju.getScale()));
            int x = (int) (kaiju.getX() + Math.cos(a) * d);
            int z = (int) (kaiju.getZ() + Math.sin(a) * d);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            SpaceCrystalEntity crystal = ModEntities.SPACE_CRYSTAL.get().create(level);
            if (crystal == null) continue;
            crystal.moveTo(x + 0.5, y, z + 0.5, kaiju.getRandom().nextFloat() * 360f, 0);
            crystal.finalizeSpawn(level, level.getCurrentDifficultyAt(crystal.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            crystal.setOwner(kaiju);
            level.addFreshEntity(crystal);
            level.sendParticles(ParticleTypes.END_ROD, x, y + 2, z, 30, 1, 2, 1, 0.1);
        }
    }
}

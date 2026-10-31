package com.reiko45.godzilla.entity.projectile;

import com.reiko45.godzilla.entity.kaiju.KingGhidorahEntity;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.HitResult;

/** The burning meteor King Ghidorah arrives in (Ghidorah, the Three-Headed Monster, 1964). */
public class GhidorahMeteorEntity extends ThrowableItemProjectile {
    public GhidorahMeteorEntity(EntityType<? extends GhidorahMeteorEntity> type, Level level) {
        super(type, level);
    }

    public GhidorahMeteorEntity(Level level, double x, double y, double z) {
        super(ModEntities.GHIDORAH_METEOR.get(), x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.MAGMA_BLOCK;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public boolean isOnFire() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            for (int i = 0; i < 4; i++) {
                level().addParticle(ParticleTypes.LARGE_SMOKE, getRandomX(2), getRandomY(), getRandomZ(2), 0, 0.05, 0);
                level().addParticle(ParticleTypes.FLAME, getRandomX(2), getRandomY(), getRandomZ(2), 0, 0, 0);
            }
            level().addParticle(ParticleTypes.LAVA, getX(), getY(), getZ(), 0, 0, 0);
        } else {
            if (tickCount % 20 == 0) {
                level().playSound(null, getX(), getY(), getZ(), net.minecraft.sounds.SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 8f, 0.4f);
            }
            if (tickCount > 600) impact();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide) impact();
    }

    private void impact() {
        if (!(level() instanceof ServerLevel server) || isRemoved()) return;
        boolean grief = KaijuDestruction.canGrief(server, this);
        server.explode(this, getX(), getY(), getZ(), 6.0f, grief, grief ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
        KingGhidorahEntity ghidorah = ModEntities.KING_GHIDORAH.get().create(server);
        if (ghidorah != null) {
            int y = server.getHeight(Heightmap.Types.MOTION_BLOCKING, getBlockX(), getBlockZ());
            ghidorah.moveTo(getX(), y + 1, getZ(), random.nextFloat() * 360f, 0);
            ghidorah.finalizeSpawn(server, server.getCurrentDifficultyAt(ghidorah.blockPosition()), MobSpawnType.EVENT, null);
            server.addFreshEntity(ghidorah);
            server.playSound(null, getX(), getY(), getZ(), ModSounds.GHIDORAH_CALL.get(), SoundSource.HOSTILE, 16f, 1f);
        }
        discard();
    }
}

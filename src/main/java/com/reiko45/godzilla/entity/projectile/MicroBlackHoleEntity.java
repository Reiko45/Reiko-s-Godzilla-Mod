package com.reiko45.godzilla.entity.projectile;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * The Dimension Tide's shot (Godzilla vs. Megaguirus, 2000): an artificial micro black hole that drags everything in
 * and erases the ground around its target. Lasts five seconds.
 */
public class MicroBlackHoleEntity extends Entity {
    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(MicroBlackHoleEntity.class, EntityDataSerializers.FLOAT);
    private static final int LIFETIME = 100;
    private boolean griefing;

    public MicroBlackHoleEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setup(float radius, boolean griefing) {
        entityData.set(DATA_RADIUS, radius);
        this.griefing = griefing;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_RADIUS, 6f);
    }

    public float getRadius() {
        return entityData.get(DATA_RADIUS) * Math.min(1f, tickCount / 20f);
    }

    @Override
    public void tick() {
        super.tick();
        float r = Math.max(0.5f, getRadius());
        if (level().isClientSide) {
            for (int i = 0; i < 45; i++) {
                double theta = random.nextDouble() * Math.PI * 2, phi = Math.acos(2 * random.nextDouble() - 1);
                double d = r * (0.6 + random.nextDouble() * 1.6);
                double x = getX() + d * Math.sin(phi) * Math.cos(theta), y = getY() + d * Math.cos(phi), z = getZ() + d * Math.sin(phi) * Math.sin(theta);
                level().addParticle(ParticleTypes.REVERSE_PORTAL, x, y, z, (getX() - x) * 0.1, (getY() - y) * 0.1, (getZ() - z) * 0.1);
            }
            for (int i = 0; i < 6; i++) {
                level().addParticle(ParticleTypes.SQUID_INK, getX() + (random.nextDouble() - 0.5) * r * 0.6, getY() + (random.nextDouble() - 0.5) * r * 0.6,
                        getZ() + (random.nextDouble() - 0.5) * r * 0.6, 0, 0, 0);
            }
            level().addParticle(ParticleTypes.DRAGON_BREATH, getX(), getY(), getZ(), (random.nextDouble() - 0.5) * 0.2, (random.nextDouble() - 0.5) * 0.2,
                    (random.nextDouble() - 0.5) * 0.2);
            return;
        }
        ServerLevel level = (ServerLevel) level();
        if (tickCount == 1) level.playSound(null, getX(), getY(), getZ(), ModSounds.BLACK_HOLE.get(), SoundSource.HOSTILE, 10f, 0.6f);
        if (tickCount % 4 == 0) level.sendParticles(ParticleTypes.SQUID_INK, getX(), getY(), getZ(), 30, r * 0.3, r * 0.3, r * 0.3, 0.02);
        Vec3 c = position();
        for (Entity e : level.getEntities(this, getBoundingBox().inflate(r * 3))) {
            if (e instanceof Player p && (p.isCreative() || p.isSpectator())) continue;
            if (e instanceof net.neoforged.neoforge.entity.PartEntity<?>) continue;
            if (e instanceof MicroBlackHoleEntity) continue;
            Vec3 to = c.subtract(e.position().add(0, e.getBbHeight() * 0.5, 0));
            double d = to.length();
            if (d > r * 3) continue;
            double pull = (e instanceof KaijuEntity ? 0.05 : 0.18) * (1.2 - d / (r * 3));
            e.setDeltaMovement(e.getDeltaMovement().add(to.normalize().scale(pull)));
            e.hurtMarked = true;
            if (d <= r + e.getBbWidth() * 0.5 && tickCount % 5 == 0 && e instanceof LivingEntity living) {
                float damage = living instanceof KaijuEntity k ? Math.max(10f, k.getMaxHealth() * 0.03f) : 8f;
                KaijuDestruction.hurtIgnoringCooldown(living, ModDamageTypes.source(level, ModDamageTypes.BLACK_HOLE, null), damage);
            } else if (d <= r && (e instanceof net.minecraft.world.entity.item.ItemEntity || e instanceof net.minecraft.world.entity.projectile.Projectile)) {
                e.discard();
            }
        }
        if (griefing) eatBlocks(level, r);
        if (tickCount >= LIFETIME) {
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
            discard();
        }
    }

    private void eatBlocks(ServerLevel level, float r) {
        int budget = 96;
        for (int i = 0; i < 160 && budget > 0; i++) {
            double theta = random.nextDouble() * Math.PI * 2, phi = Math.acos(2 * random.nextDouble() - 1);
            double d = r * Math.cbrt(random.nextDouble());
            BlockPos pos = BlockPos.containing(getX() + d * Math.sin(phi) * Math.cos(theta), getY() + d * Math.cos(phi), getZ() + d * Math.sin(phi) * Math.sin(theta));
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || !KaijuDestruction.isBreakable(level, pos, state)) continue;
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            budget--;
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 256 * 256;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(DATA_RADIUS, tag.getFloat("Radius"));
        griefing = tag.getBoolean("Griefing");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Radius", entityData.get(DATA_RADIUS));
        tag.putBoolean("Griefing", griefing);
    }
}

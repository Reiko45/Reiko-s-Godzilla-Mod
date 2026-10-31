package com.reiko45.godzilla.util;

import com.reiko45.godzilla.GodzillaConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.EventHooks;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Shared helpers for breaking blocks and hurting everything in a beam's path. */
public final class KaijuDestruction {
    private KaijuDestruction() {}

    /** Whether this entity may break blocks right now (config + mobGriefing game rule). */
    public static boolean canGrief(Level level, Entity entity) {
        return GodzillaConfig.get(GodzillaConfig.KAIJU_GRIEFING, true) && EventHooks.canEntityGrief(level, entity);
    }

    public static boolean isBreakable(Level level, BlockPos pos, BlockState state) {
        if (state.isAir() || state.getBlock() instanceof LiquidBlock) return false;
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0) return false;
        double maxRes = GodzillaConfig.get(GodzillaConfig.MAX_BLOCK_RESISTANCE, 1200.0);
        return state.getExplosionResistance(level, pos, null) < maxRes;
    }

    /**
     * Removes breakable blocks inside the box. Returns how many were removed.
     * Container contents still drop (vanilla onRemove handles that).
     */
    public static int breakBox(ServerLevel level, AABB box, int budget) {
        int broken = 0;
        int minX = Mth.floor(box.minX), maxX = Mth.floor(box.maxX);
        int minY = Mth.floor(box.minY), maxY = Mth.floor(box.maxY);
        int minZ = Mth.floor(box.minZ), maxZ = Mth.floor(box.maxZ);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = maxY; y >= minY && broken < budget; y--) {
            for (int x = minX; x <= maxX && broken < budget; x++) {
                for (int z = minZ; z <= maxZ && broken < budget; z++) {
                    pos.set(x, y, z);
                    if (!level.isLoaded(pos)) continue;
                    BlockState state = level.getBlockState(pos);
                    if (!isBreakable(level, pos, state)) continue;
                    removeBlock(level, pos.immutable(), state, broken % 7 == 0);
                    broken++;
                }
            }
        }
        return broken;
    }

    /** Breaks a rough sphere of blocks, optionally leaving fires behind. */
    public static int breakSphere(ServerLevel level, Vec3 center, float radius, int budget, boolean fire, RandomSource random) {
        int broken = 0;
        int r = Mth.ceil(radius);
        BlockPos c = BlockPos.containing(center);
        float r2 = radius * radius;
        for (int dy = r; dy >= -r && broken < budget; dy--) {
            for (int dx = -r; dx <= r && broken < budget; dx++) {
                for (int dz = -r; dz <= r && broken < budget; dz++) {
                    float jitter = random.nextFloat() * radius * 0.6f;
                    if (dx * dx + dy * dy + dz * dz > r2 + jitter) continue;
                    BlockPos pos = c.offset(dx, dy, dz);
                    if (!level.isLoaded(pos)) continue;
                    BlockState state = level.getBlockState(pos);
                    if (!isBreakable(level, pos, state)) continue;
                    removeBlock(level, pos, state, broken % 5 == 0);
                    broken++;
                }
            }
        }
        if (fire && GodzillaConfig.get(GodzillaConfig.BEAM_FIRE, true)) {
            for (int i = 0; i < 4; i++) {
                BlockPos pos = c.offset(random.nextInt(r * 2 + 1) - r, random.nextInt(r + 1) - r / 2, random.nextInt(r * 2 + 1) - r);
                if (level.isLoaded(pos) && level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isSolidRender(level, pos.below())) {
                    level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
                }
            }
        }
        return broken;
    }

    private static void removeBlock(ServerLevel level, BlockPos pos, BlockState state, boolean effects) {
        if (effects) level.levelEvent(2001, pos, Block.getId(state));
        level.removeBlock(pos, false);
    }

    /**
     * Hurts every living thing whose (slightly inflated) hitbox crosses the segment from -> to.
     * Multipart hits are forwarded to the parent once.
     */
    public static void damageAlongSegment(Entity source, Vec3 from, Vec3 to, double radius, DamageSource damage, float amount,
                                          Predicate<LivingEntity> filter, Consumer<LivingEntity> onHit) {
        Level level = source.level();
        AABB area = new AABB(from, to).inflate(radius + 1.0);
        Set<Entity> done = new HashSet<>();
        for (Entity e : level.getEntities(source, area)) {
            Entity target = e instanceof PartEntity<?> part ? part.getParent() : e;
            if (target == source || !(target instanceof LivingEntity living) || !living.isAlive()) continue;
            if (done.contains(target)) continue;
            AABB box = e.getBoundingBox().inflate(radius);
            if (!box.contains(from) && box.clip(from, to).isEmpty()) continue;
            if (!filter.test(living)) continue;
            done.add(target);
            hurtIgnoringCooldown(living, damage, amount);
            onHit.accept(living);
        }
    }

    /** Applies damage even if the target was hit a few ticks ago (beams hit many times per second). */
    public static void hurtIgnoringCooldown(LivingEntity target, DamageSource damage, float amount) {
        if (target instanceof Player) {
            amount *= (float) GodzillaConfig.get(GodzillaConfig.PLAYER_DAMAGE_MULTIPLIER, 1.0);
            if (amount <= 0) return;
        }
        target.invulnerableTime = 0;
        target.hurt(damage, amount);
    }
}

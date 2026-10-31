package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/** Hurricane-force wing beats (Mothra, Rodan, King Ghidorah): knocks everything in front back and strips leaves. */
public class WindGustAbility extends KaijuAbility {
    private final float strength;

    public WindGustAbility(float strength) {
        super("wind_gust", 10, 40, 220, "charge", "gust");
        this.strength = strength;
        this.minRange = 2;
        this.maxRange = 28;
        this.useChance = 0.03f;
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (target != null) KaijuAbility.faceTowards(kaiju, target.getX(), target.getZ(), 4f);
        if (!(kaiju.level() instanceof ServerLevel level) || tick % 4 != 0) return;
        double range = maxRange * kaiju.getScale() + kaiju.getBbWidth();
        Vec3 forward = Vec3.directionFromRotation(0, kaiju.yBodyRot);
        if (tick % 12 == 0) {
            level.playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.HOSTILE, 5f, 0.6f);
        }
        for (int i = 0; i < 12; i++) {
            Vec3 p = kaiju.position().add(forward.scale(kaiju.getRandom().nextDouble() * range))
                    .add((kaiju.getRandom().nextDouble() - 0.5) * range * 0.6, kaiju.getRandom().nextDouble() * kaiju.getBbHeight(), (kaiju.getRandom().nextDouble() - 0.5) * range * 0.6);
            level.sendParticles(ParticleTypes.CLOUD, p.x, p.y, p.z, 1, forward.x, 0, forward.z, 0.6);
        }
        for (Entity e : level.getEntities(kaiju, kaiju.getBoundingBox().inflate(range))) {
            if (e instanceof Projectile projectile) {
                projectile.setDeltaMovement(forward.scale(1.5));
                continue;
            }
            if (!(e instanceof LivingEntity living) || !AreaAbility.isVictim(kaiju, living) || !AreaAbility.inCone(kaiju, living, 0.6)) continue;
            double falloff = 1.0 - Math.min(1.0, e.distanceTo(kaiju) / range);
            double knock = strength * (0.4 + falloff) * (1.0 - living.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE) * 0.8);
            living.push(forward.x * knock, 0.25 * knock, forward.z * knock);
            living.hurtMarked = true;
            if (tick % 12 == 0) living.hurt(kaiju.damageSources().mobAttack(kaiju), 2.0f);
        }
        if (KaijuDestruction.canGrief(level, kaiju)) {
            for (int i = 0; i < 10; i++) {
                Vec3 p = kaiju.position().add(forward.scale(kaiju.getRandom().nextDouble() * range))
                        .add((kaiju.getRandom().nextDouble() - 0.5) * range * 0.5, kaiju.getRandom().nextDouble() * 8 - 2, (kaiju.getRandom().nextDouble() - 0.5) * range * 0.5);
                BlockPos pos = BlockPos.containing(p);
                BlockState state = level.getBlockState(pos);
                if (state.is(BlockTags.LEAVES) || state.is(BlockTags.FLOWERS) || state.is(BlockTags.REPLACEABLE) || state.is(net.neoforged.neoforge.common.Tags.Blocks.GLASS_BLOCKS)
                        || state.is(net.neoforged.neoforge.common.Tags.Blocks.GLASS_PANES)) {
                    level.destroyBlock(pos, false, kaiju);
                }
            }
        }
    }
}

package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.util.KaijuDestruction;
import com.reiko45.godzilla.world.Emp;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Megaguirus beats her wings fast enough to send out ultrasonic shockwaves: a ring that knocks everything down,
 * shatters glass and scrambles electronics (it threw the Dimension Tide out of orbit).
 */
public class SonicWaveAbility extends KaijuAbility {
    private final float radius;

    public SonicWaveAbility(float radius) {
        super("sonic_wave", 16, 24, 300, "charge", "gust");
        this.radius = radius;
        this.minRange = 0;
        this.maxRange = 20;
        this.useChance = 0.03f;
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (!(kaiju.level() instanceof ServerLevel level)) return;
        double max = radius * kaiju.getScale() + kaiju.getBbWidth();
        double ring = max * (tick + 1) / activeTicks;
        for (int i = 0; i < 36; i++) {
            double a = Math.PI * 2 * i / 36;
            level.sendParticles(ParticleTypes.SONIC_BOOM, kaiju.getX() + Math.cos(a) * ring, kaiju.getY(0.5), kaiju.getZ() + Math.sin(a) * ring, 1, 0, 0, 0, 0);
        }
        if (tick == 0) {
            level.playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 6f, 1.4f);
            Emp.pulse(level, kaiju.position(), max, 120, false);
        }
        for (LivingEntity e : AreaAbility.victims(kaiju, max)) {
            double d = e.distanceTo(kaiju);
            if (Math.abs(d - ring) > max / activeTicks + 1.5) continue;
            e.hurt(ModDamageTypes.source(level, ModDamageTypes.SONIC_BOOM, kaiju), e instanceof KaijuEntity ? 10f : 6f);
            e.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0));
            Vec3 away = e.position().subtract(kaiju.position()).multiply(1, 0, 1).normalize();
            e.push(away.x * 1.2, 0.4, away.z * 1.2);
            e.hurtMarked = true;
        }
        if (tick % 4 == 0 && KaijuDestruction.canGrief(level, kaiju)) {
            for (int i = 0; i < 20; i++) {
                double a = kaiju.getRandom().nextDouble() * Math.PI * 2;
                BlockPos pos = BlockPos.containing(kaiju.getX() + Math.cos(a) * ring, kaiju.getY() + kaiju.getRandom().nextInt(12) - 2, kaiju.getZ() + Math.sin(a) * ring);
                BlockState state = level.getBlockState(pos);
                if (state.is(net.neoforged.neoforge.common.Tags.Blocks.GLASS_BLOCKS) || state.is(net.neoforged.neoforge.common.Tags.Blocks.GLASS_PANES)) {
                    level.destroyBlock(pos, false, kaiju);
                }
            }
        }
    }
}

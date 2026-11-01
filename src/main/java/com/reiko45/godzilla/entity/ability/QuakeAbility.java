package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * A ground-shaking slam that stuns everything nearby: Behemoth's tusks, Methuselah's stomp. Kaiju caught in it lose
 * whatever attack they were winding up.
 */
public class QuakeAbility extends KaijuAbility {
    private final float radius;
    private final float damageMultiplier;

    public QuakeAbility(String name, float radius, float damageMultiplier, int cooldown) {
        super(name, 25, 15, cooldown, "charge", "slam");
        this.radius = radius;
        this.damageMultiplier = damageMultiplier;
        this.minRange = 0;
        this.maxRange = radius * 0.8;
        this.useChance = 0.05f;
    }

    @Override
    public void tickCharge(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (target != null) faceTowards(kaiju, target.getX(), target.getZ(), 10f);
    }

    @Override
    public void activate(KaijuEntity kaiju, @Nullable LivingEntity target) {
        if (!(kaiju.level() instanceof ServerLevel level)) return;
        double r = radius * kaiju.getScale() + kaiju.getBbWidth() * 0.5;
        level.playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 6f, 0.4f);
        BlockState ground = level.getBlockState(kaiju.blockPosition().below());
        if (!ground.isAir()) {
            for (int i = 0; i < 48; i++) {
                double a = i * Math.PI * 2 / 48;
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), kaiju.getX() + Math.cos(a) * r * 0.6, kaiju.getY() + 0.3,
                        kaiju.getZ() + Math.sin(a) * r * 0.6, 4, 0.5, 0.3, 0.5, 0.1);
            }
        }
        float damage = (float) kaiju.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;
        for (LivingEntity e : AreaAbility.victims(kaiju, r)) {
            if (!e.onGround() && !(e instanceof KaijuEntity)) continue;
            e.hurt(kaiju.damageSources().mobAttack(kaiju), damage);
            Vec3 away = e.position().subtract(kaiju.position()).multiply(1, 0, 1).normalize();
            e.push(away.x * 0.6, 0.5, away.z * 0.6);
            if (e instanceof KaijuEntity k) {
                if (k.isAbilityActive()) k.stopAbility();
            } else {
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 3));
                e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1));
                e.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
            }
        }
        if (KaijuDestruction.canGrief(level, kaiju)) {
            KaijuDestruction.breakSphere(level, kaiju.position().add(Vec3.directionFromRotation(0, kaiju.yBodyRot).scale(kaiju.getBbWidth())),
                    (float) Math.min(5, kaiju.getScale() * 1.5), GodzillaConfig.get(GodzillaConfig.MAX_BLOCKS_PER_TICK, 48), false, kaiju.getRandom());
        }
    }
}

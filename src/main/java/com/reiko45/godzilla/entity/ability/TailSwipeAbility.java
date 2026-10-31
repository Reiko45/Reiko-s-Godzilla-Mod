package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.KaijuEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/** A spinning tail strike that hits everything close by and flings it away. */
public class TailSwipeAbility extends KaijuAbility {
    public TailSwipeAbility() {
        super("tail_swipe", 6, 16, 160, "charge", "tail");
        this.minRange = 0;
        this.maxRange = 6;
        this.useChance = 0.06f;
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        kaiju.setYRot(kaiju.getYRot() + 22.5f);
        kaiju.yBodyRot = kaiju.getYRot();
        if (tick != 8) return;
        float damage = (float) kaiju.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.8f;
        kaiju.level().playSound(null, kaiju.getX(), kaiju.getY(), kaiju.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 4f, 0.5f);
        for (LivingEntity e : AreaAbility.victims(kaiju, 7 * kaiju.getScale())) {
            if (e.hurt(kaiju.damageSources().mobAttack(kaiju), damage)) {
                Vec3 away = e.position().subtract(kaiju.position()).multiply(1, 0, 1).normalize();
                e.push(away.x * 2.0, 0.6, away.z * 2.0);
            }
        }
    }
}

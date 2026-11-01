package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.KaijuEntity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/** A thunderous roar that staggers small creatures nearby (Slowness + Weakness). */
public class RoarAbility extends KaijuAbility {
    private final Supplier<SoundEvent> sound;

    public RoarAbility(Supplier<SoundEvent> sound) {
        super("roar", 0, 40, 600, "roar", "roar");
        this.sound = sound;
        this.minRange = 0;
        this.maxRange = 48;
        this.useChance = 0.01f;
    }

    @Override
    public void activate(KaijuEntity kaiju, @Nullable LivingEntity target) {
        kaiju.level().playSound(null, kaiju.getX(), kaiju.getEyeY(), kaiju.getZ(), sound.get(), SoundSource.HOSTILE,
                8.0f + kaiju.getScale() * 2, 0.9f + kaiju.getRandom().nextFloat() * 0.2f);
        for (KaijuEntity other : kaiju.level().getEntitiesOfClass(KaijuEntity.class, kaiju.getBoundingBox().inflate(96), k -> k != kaiju)) {
            other.onRoarHeard(kaiju);
        }
        for (LivingEntity e : AreaAbility.victims(kaiju, 24 * kaiju.getScale())) {
            if (e instanceof KaijuEntity) continue;
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
            e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
        }
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (target != null) kaiju.getLookControl().setLookAt(target, 10f, 10f);
    }
}

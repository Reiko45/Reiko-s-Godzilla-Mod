package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.projectile.SilkShotEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/** Mothra larva's silk spray: sticky threads that slow and bind whatever they hit. */
public class SilkSprayAbility extends KaijuAbility {
    public SilkSprayAbility() {
        super("silk_spray", 10, 30, 140, "charge", "spray");
        this.minRange = 2;
        this.maxRange = 28;
        this.useChance = 0.05f;
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (target == null) return;
        KaijuAbility.faceTowards(kaiju, target.getX(), target.getZ(), 8f);
        kaiju.getLookControl().setLookAt(target, 10f, 10f);
        if (tick % 4 != 0) return;
        Vec3 mouth = kaiju.getBeamOrigin(0, 1f);
        SilkShotEntity shot = new SilkShotEntity(kaiju.level(), kaiju);
        shot.setPos(mouth.x, mouth.y, mouth.z);
        Vec3 to = target.getBoundingBox().getCenter().subtract(mouth);
        shot.shoot(to.x, to.y + to.horizontalDistance() * 0.05, to.z, 1.6f, 6f);
        kaiju.level().addFreshEntity(shot);
        kaiju.level().playSound(null, mouth.x, mouth.y, mouth.z, SoundEvents.LLAMA_SPIT, SoundSource.HOSTILE, 2f, 0.6f);
    }
}

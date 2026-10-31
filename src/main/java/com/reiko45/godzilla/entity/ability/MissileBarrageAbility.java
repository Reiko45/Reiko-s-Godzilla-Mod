package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.projectile.KaijuMissileEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/** Mechagodzilla's finger missiles: a rapid volley of small explosive rockets from both hands. */
public class MissileBarrageAbility extends KaijuAbility {
    private final Vec3 leftHand;
    private final Vec3 rightHand;

    public MissileBarrageAbility(Vec3 leftHand, Vec3 rightHand) {
        super("finger_missiles", 10, 30, 160, "charge", "missiles");
        this.leftHand = leftHand;
        this.rightHand = rightHand;
        this.minRange = 6;
        this.maxRange = 48;
        this.useChance = 0.04f;
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        if (target == null) return;
        KaijuAbility.faceTowards(kaiju, target.getX(), target.getZ(), 6f);
        if (tick % 3 != 0) return;
        Vec3 hand = (tick / 3) % 2 == 0 ? leftHand : rightHand;
        Vec3 from = kaiju.position().add(hand.scale(kaiju.getScale()).yRot(-kaiju.yBodyRot * net.minecraft.util.Mth.DEG_TO_RAD));
        KaijuMissileEntity missile = new KaijuMissileEntity(kaiju.level(), kaiju);
        missile.setPos(from.x, from.y, from.z);
        Vec3 to = target.getBoundingBox().getCenter().subtract(from);
        missile.shoot(to.x, to.y, to.z, 1.4f, 5f);
        kaiju.level().addFreshEntity(missile);
        kaiju.level().playSound(null, from.x, from.y, from.z, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.HOSTILE, 3f, 0.7f);
    }
}

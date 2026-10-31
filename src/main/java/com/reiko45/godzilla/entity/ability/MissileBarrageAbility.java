package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.projectile.KaijuMissileEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * A volley of rockets from one or two launch points: Showa Mechagodzilla's finger missiles, Heisei Mechagodzilla's
 * paralyzer missiles, the Super X series' cadmium and freezer missiles.
 */
public class MissileBarrageAbility extends KaijuAbility {
    private final Vec3 leftHand;
    private final Vec3 rightHand;
    private final KaijuMissileEntity.Payload payload;
    private int interval = 3;

    public MissileBarrageAbility(Vec3 leftHand, Vec3 rightHand) {
        this("finger_missiles", leftHand, rightHand, KaijuMissileEntity.Payload.EXPLOSIVE, 160);
    }

    public MissileBarrageAbility(String name, Vec3 leftHand, Vec3 rightHand, KaijuMissileEntity.Payload payload, int cooldown) {
        super(name, 10, 30, cooldown, "charge", "missiles");
        this.leftHand = leftHand;
        this.rightHand = rightHand;
        this.payload = payload;
        this.minRange = 6;
        this.maxRange = 48;
        this.useChance = 0.04f;
    }

    public MissileBarrageAbility interval(int ticks) {
        this.interval = ticks;
        return this;
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        Vec3 aim = aimPoint(kaiju, target);
        if (kaiju.getPilot() == null) KaijuAbility.faceTowards(kaiju, aim.x, aim.z, 6f);
        if (tick % interval != 0) return;
        Vec3 hand = (tick / interval) % 2 == 0 ? leftHand : rightHand;
        Vec3 from = kaiju.position().add(hand.scale(kaiju.getScale()).yRot(-kaiju.yBodyRot * net.minecraft.util.Mth.DEG_TO_RAD));
        KaijuMissileEntity missile = new KaijuMissileEntity(kaiju.level(), kaiju, payload);
        missile.setPos(from.x, from.y, from.z);
        Vec3 to = aim.subtract(from);
        missile.shoot(to.x, to.y, to.z, 1.6f, 4f);
        kaiju.level().addFreshEntity(missile);
        kaiju.level().playSound(null, from.x, from.y, from.z, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.HOSTILE, 3f, 0.7f);
    }
}

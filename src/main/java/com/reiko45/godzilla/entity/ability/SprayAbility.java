package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.KaijuEntity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

/** Sprays a stream of projectiles from the mouth (Mothra larva's silk, Biollante's acid sap). */
public class SprayAbility extends KaijuAbility {
    private final Function<KaijuEntity, Projectile> factory;
    private final Supplier<SoundEvent> sound;
    private final float velocity;
    private final float inaccuracy;
    private int interval = 4;

    public SprayAbility(String name, Function<KaijuEntity, Projectile> factory, Supplier<SoundEvent> sound, float velocity, float inaccuracy,
                        int activeTicks, int cooldown) {
        super(name, 10, activeTicks, cooldown, "charge", "spray");
        this.factory = factory;
        this.sound = sound;
        this.velocity = velocity;
        this.inaccuracy = inaccuracy;
        this.minRange = 2;
        this.maxRange = 28;
        this.useChance = 0.05f;
    }

    public SprayAbility interval(int ticks) {
        this.interval = ticks;
        return this;
    }

    @Override
    public void tickActive(KaijuEntity kaiju, @Nullable LivingEntity target, int tick) {
        Vec3 aim = aimPoint(kaiju, target);
        if (kaiju.getPilot() == null) KaijuAbility.faceTowards(kaiju, aim.x, aim.z, 8f);
        if (target != null) kaiju.getLookControl().setLookAt(target, 10f, 10f);
        if (tick % interval != 0) return;
        Vec3 mouth = kaiju.getBeamOrigin(0, 1f);
        Projectile shot = factory.apply(kaiju);
        shot.setPos(mouth.x, mouth.y, mouth.z);
        Vec3 to = aim.subtract(mouth);
        shot.shoot(to.x, to.y + to.horizontalDistance() * 0.06, to.z, velocity, inaccuracy);
        kaiju.level().addFreshEntity(shot);
        kaiju.level().playSound(null, mouth.x, mouth.y, mouth.z, sound.get(), SoundSource.HOSTILE, 2f, 0.6f);
    }
}

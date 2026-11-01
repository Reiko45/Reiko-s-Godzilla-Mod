package com.reiko45.godzilla.entity.showa;

import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.entity.projectile.LightBombEntity;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.world.AlienSignal;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/**
 * Varan the Unbelievable (1958), worshipped as the mountain god Baradagi. Skin membranes between its limbs let it leap
 * from high places and glide; it's at home in the water too.
 * <p>
 * Its weakness is light: it can't resist a glowing flare and swallows it. Throw a Light Bomb near Varan and it will
 * dive for it and gulp it down, and the explosive inside does terrible damage.
 */
public class VaranEntity extends FlyingKaijuEntity {
    public VaranEntity(EntityType<? extends VaranEntity> type, Level level) {
        super(type, level);
        this.xpReward = 350;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(550, 16, 8, 24, 0.25).add(Attributes.FLYING_SPEED, 0.55);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.TITAN;
    }

    @Override
    public boolean controllableBy(AlienSignal signal) {
        return signal == AlienSignal.KILAAK;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new DiveAbility(1.8f));
        list.add(new TailSwipeAbility());
        list.add(new HoldAbility("jaws", "grab", HoldAbility.Mode.MOUTH, 30, 6, 0.4f, 0f, 220));
        list.add(new WindGustAbility(1.6f));
        list.add(new RoarAbility(ModSounds.VARAN_CRY));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 1.8f, 1.6f, new Vec3(0, 2.6, 4.2), 1.3f),
                new KaijuPart(this, "tail", 1.8f, 1.4f, new Vec3(0, 1.4, -5.0), 0.8f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 2.8, 5.0);
    }

    @Override
    public int landChance() {
        return 160;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (tickCount % 10 != 0) return;
        // A flare nearby: it can't help itself.
        LightBombEntity flare = level().getEntitiesOfClass(LightBombEntity.class, getBoundingBox().inflate(48), LightBombEntity::isLit).stream()
                .min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
        if (flare == null) return;
        if (distanceTo(flare) < getBbWidth() * 0.8 + 3) {
            flare.swallow(this);
        } else {
            setTarget(null);
            if (isAbilityActive()) stopAbility();
            getNavigation().moveTo(flare.getX(), flare.getY(), flare.getZ(), 1.4);
            if (isFlying()) getMoveControl().setWantedPosition(flare.getX(), flare.getY(), flare.getZ(), 1.4);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.VARAN_CRY.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.VARAN_CRY.get();
    }
}

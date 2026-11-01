package com.reiko45.godzilla.entity.showa;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.world.AlienSignal;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Gorosaurus (King Kong Escapes, Destroy All Monsters): an allosaurus-like dinosaur from Mondo Island who later lived
 * on Monster Island. Its famous "kangaroo kick" springs off the tail and slams both feet into the target.
 */
public class GorosaurusEntity extends KaijuEntity {
    public GorosaurusEntity(EntityType<? extends GorosaurusEntity> type, Level level) {
        super(type, level);
        this.xpReward = 300;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(450, 16, 6, 24, 0.28);
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
        list.add(new LeapAbility("kangaroo_kick", 1.8, 2.0f, 200));
        list.add(new TailSwipeAbility());
        list.add(new HoldAbility("jaws", "grab", HoldAbility.Mode.MOUTH, 30, 6, 0.4f, 0f, 220));
        list.add(new RoarAbility(ModSounds.GOROSAURUS_ROAR));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 2.2f, 2.0f, new Vec3(0, 8.0, 2.4), 1.4f),
                new KaijuPart(this, "tail", 2.2f, 2.0f, new Vec3(0, 1.4, -4.2), 0.8f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 8.8, 3.0);
    }

    @Override
    protected double variantScale() {
        return 0.74;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.GOROSAURUS_ROAR.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GOROSAURUS_ROAR.get();
    }
}

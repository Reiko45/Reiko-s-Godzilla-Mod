package com.reiko45.godzilla.entity.kaiju;

import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.DiveAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.RoarAbility;
import com.reiko45.godzilla.entity.ability.WindGustAbility;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Rodan, the supersonic pteranodon born in a volcano. Flies faster than any other kaiju, dives through its prey
 * trailing a sonic boom, and blasts enemies with the wind from its wings.
 */
public class RodanEntity extends FlyingKaijuEntity {
    public RodanEntity(EntityType<? extends RodanEntity> type, Level level) {
        super(type, level);
        this.xpReward = 350;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(500, 12, 6, 20, 0.26)
                .add(Attributes.FLYING_SPEED, 0.95)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    @Override
    public KaijuFaction getFaction() {
        return KaijuFaction.TITAN;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new DiveAbility(2.2f));
        list.add(new WindGustAbility(2.0f));
        list.add(new RoarAbility(ModSounds.RODAN_SCREECH));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 1.6f, 1.6f, new Vec3(0, 5.0, 2.2), 1.4f),
                new KaijuPart(this, "left_wing", 6.0f, 1.2f, new Vec3(-5.5, 3.6, 0), 0.7f),
                new KaijuPart(this, "right_wing", 6.0f, 1.2f, new Vec3(5.5, 3.6, 0), 0.7f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 5.6, 2.8);
    }

    @Override
    public int landChance() {
        return 900;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.RODAN_SCREECH.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.RODAN_SCREECH.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.RODAN_SCREECH.get();
    }
}

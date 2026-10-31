package com.reiko45.godzilla.entity.kaiju;

import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * SpaceGodzilla (1994): born from Godzilla cells carried into space through a black hole.
 * <ul>
 *     <li>Corona Beam: an arcing beam that homes in on its target.</li>
 *     <li>Crystal Field: raises crystal towers that power it. While any stand, it regenerates quickly.</li>
 *     <li>Gravity tornado: lifts and flings everything small around it.</li>
 *     <li>Photon Reactive Shield: bounces beams back at whoever fired them.</li>
 *     <li>Shoulder crystals are its weak point (double damage).</li>
 *     <li>Crosses long distances in a crystal-encased flying form.</li>
 * </ul>
 */
public class SpaceGodzillaEntity extends FlyingKaijuEntity {
    private int poweringCrystals;

    public SpaceGodzillaEntity(EntityType<? extends SpaceGodzillaEntity> type, Level level) {
        super(type, level);
        this.xpReward = 700;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(1024, 22, 10, 30, 0.2).add(Attributes.FLYING_SPEED, 0.8);
    }

    @Override
    public KaijuFaction getFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("corona_beam", BeamStyle.CORONA, 1, 0.8f, ModDamageTypes.CORONA_BEAM, 30, 60, 200)
                .damage(12f, 4).blocks(1.8f, false).tracking(0.35f, 0f).nuclear()
                .sounds(ModSounds.SPACE_BEAM_CHARGE, ModSounds.ATOMIC_BREATH_FIRE).chargeParticle(ParticleTypes.END_ROD)
                .range(4, 56));
        list.add(new CrystalFieldAbility());
        list.add(new GravityTornadoAbility());
        list.add(new ReflectShieldAbility("photon_shield", ParticleTypes.END_ROD));
        list.add(new TailSwipeAbility());
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 2.0f, 2.0f, new Vec3(0, 8.3, 2.2), 1.4f),
                new KaijuPart(this, "left_crystal", 1.6f, 2.4f, new Vec3(-2.2, 7.2, -0.2), 2.0f),
                new KaijuPart(this, "right_crystal", 1.6f, 2.4f, new Vec3(2.2, 7.2, -0.2), 2.0f),
                new KaijuPart(this, "tail", 2.6f, 2.4f, new Vec3(0, 1.4, -4.2), 0.8f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 9.0, 2.9);
    }

    @Override
    protected double variantScale() {
        return 2.4;
    }

    @Override
    protected double baseStepHeight() {
        return 3.0;
    }

    @Override
    public boolean prefersFlightInCombat() {
        return false;
    }

    @Override
    public int landChance() {
        return 200;
    }

    @Override
    public boolean reflectsBeams() {
        return ReflectShieldAbility.isActive(this);
    }

    @Override
    protected float regenPerSecond() {
        return poweringCrystals > 0 ? 3f + poweringCrystals : 0.5f;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (tickCount % 40 == 0) {
            poweringCrystals = (int) level().getEntitiesOfClass(SpaceCrystalEntity.class, getBoundingBox().inflate(96), c -> c.isPowering(this)).size();
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.SPACEGODZILLA_ROAR.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.AMETHYST_BLOCK_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SPACEGODZILLA_ROAR.get();
    }
}

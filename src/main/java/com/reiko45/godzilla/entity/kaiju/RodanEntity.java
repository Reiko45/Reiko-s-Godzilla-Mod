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
    public static final int SHOWA = 0;
    public static final int FIRE = 1;

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
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.TITAN;
    }

    /** Final Wars Rodan was a Xilien weapon (M-Base); the Kilaaks controlled the Showa Rodan. */
    @Override
    public boolean hasMBase() {
        return true;
    }

    @Override
    public boolean controllableBy(com.reiko45.godzilla.world.AlienSignal signal) {
        return signal == com.reiko45.godzilla.world.AlienSignal.KILAAK || super.controllableBy(signal);
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new DiveAbility(2.2f));
        list.add(new com.reiko45.godzilla.entity.ability.BeamAbility("uranium_heat_beam", com.reiko45.godzilla.entity.BeamStyle.URANIUM, 1, 0.5f,
                com.reiko45.godzilla.registry.ModDamageTypes.URANIUM_HEAT_BEAM, 15, 40, 180)
                .damage(9f, 4).blocks(1.2f, true).nuclear().sounds(ModSounds.RODAN_SCREECH, ModSounds.ATOMIC_BREATH_FIRE)
                .chargeParticle(net.minecraft.core.particles.ParticleTypes.FLAME)
                .range(4, 44).when(k -> k.getVariant() == FIRE));
        list.add(new WindGustAbility(2.0f));
        list.add(new RoarAbility(ModSounds.RODAN_SCREECH));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 1.6f, 1.6f, new Vec3(0, 4.4, 1.7), 1.4f),
                new KaijuPart(this, "left_wing", 6.0f, 1.2f, new Vec3(-4.5, 2.8, 0), 0.7f),
                new KaijuPart(this, "right_wing", 6.0f, 1.2f, new Vec3(4.5, 2.8, 0), 0.7f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 4.45, 3.05);
    }

    @Override
    public int landChance() {
        return 900;
    }

    @javax.annotation.Nullable
    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty,
                                                                  net.minecraft.world.entity.MobSpawnType type, @javax.annotation.Nullable net.minecraft.world.entity.SpawnGroupData data) {
        setVariant(random.nextInt(2));
        return super.finalizeSpawn(level, difficulty, type, data);
    }

    @Override
    public net.minecraft.network.chat.Component getTypeName() {
        return getVariant() == FIRE ? net.minecraft.network.chat.Component.translatable("entity.reikogodzilla.fire_rodan") : super.getTypeName();
    }

    /**
     * Godzilla vs. Mechagodzilla II: a dying Fire Rodan gives its life energy to Godzilla, who gains the Red Spiral Ray.
     */
    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (getVariant() == FIRE && level() instanceof net.minecraft.server.level.ServerLevel server) {
            server.getEntitiesOfClass(GodzillaEntity.class, getBoundingBox().inflate(64)).stream()
                    .min(java.util.Comparator.comparingDouble(this::distanceToSqr))
                    .ifPresent(g -> {
                        server.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME, getX(), getY(0.5), getZ(), 80, 2, 2, 2, 0.3);
                        g.absorbRodanEnergy(6000);
                    });
        }
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

package com.reiko45.godzilla.entity.showa;

import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ShrineGuardian;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.world.AlienSignal;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Baragon.
 * <ul>
 *     <li>Showa (Frankenstein vs. Baragon, 1965): a burrowing reptile with a glowing horn and a magma heat ray that
 *     loosens the earth. It hides from daylight and comes out at night to prey on livestock.</li>
 *     <li>GMK (2001): Baragon, God of the Earth, one of the Guardian Monsters. No heat ray, but it burrows, leaps and
 *     bites, and rises to fight GMK Godzilla. Desecrate its shrine and it comes for you instead.</li>
 * </ul>
 */
public class BaragonEntity extends KaijuEntity implements ShrineGuardian {
    public static final int SHOWA = 0;
    public static final int GMK = 1;
    private final Vengeance vengeance = new Vengeance();

    public BaragonEntity(EntityType<? extends BaragonEntity> type, Level level) {
        super(type, level);
        this.xpReward = 300;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(350, 14, 6, 20, 0.28);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return getVariant() == GMK ? KaijuFaction.GUARDIAN : KaijuFaction.TITAN;
    }

    @Override
    public boolean controllableBy(AlienSignal signal) {
        return signal == AlienSignal.KILAAK && getVariant() == SHOWA;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("magma_ray", BeamStyle.MAGMA, 1, 0.4f, ModDamageTypes.MAGMA_RAY, 20, 30, 200)
                .damage(8f, 4).blocks(1.6f, true).chargeParticle(ParticleTypes.FLAME).range(4, 32).when(k -> k.getVariant() == SHOWA));
        list.add(new BurrowAbility("burrow", 1.0, 1.4f, 400));
        list.add(new LeapAbility("leap", 1.5, 1.4f, 200));
        list.add(new RoarAbility(ModSounds.BARAGON_ROAR));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{new KaijuPart(this, "head", 2.0f, 1.8f, new Vec3(0, 2.8, 3.8), 1.3f)};
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 3.2, 4.8);
    }

    @Override
    protected double variantScale() {
        return getVariant() == GMK ? 1.3 : 1.1;
    }

    @Override
    protected void applyVariantAttributes() {
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(getVariant() == GMK ? 520 : 350);
        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(getVariant() == GMK ? 24 : 20);
    }

    @Override
    public Component getTypeName() {
        return Component.translatable(getVariant() == GMK ? "entity.reikogodzilla.baragon_gmk" : "entity.reikogodzilla.baragon");
    }

    /** Showa Baragon hunts livestock at night; GMK Baragon hunts whoever desecrated its shrine. */
    @Override
    public boolean wantsToAttack(LivingEntity other) {
        if (vengeance.isTarget(other)) return true;
        if (getVariant() == SHOWA && !level().isDay() && (other instanceof Animal || other instanceof AbstractVillager)
                && other.isAlive() && !isConfused() && !isCalmed()) {
            return true;
        }
        return super.wantsToAttack(other);
    }

    @Override
    public void awaken(@Nullable Player vandal) {
        if (vandal != null) vengeance.set(vandal, 2400);
        triggerAnim("action", "roar");
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        vengeance.tick(this);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (getVariant() == GMK && level() instanceof net.minecraft.server.level.ServerLevel server) {
            com.reiko45.godzilla.entity.millennium.GmkMothraEntity.passOn(this, server);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        vengeance.save(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        vengeance.load(tag);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.BARAGON_ROAR.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.BARAGON_ROAR.get();
    }
}

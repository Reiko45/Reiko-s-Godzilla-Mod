package com.reiko45.godzilla.entity.millennium;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.ability.BeamAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.world.AlienControl;
import com.reiko45.godzilla.world.AlienSignal;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
 * A Xilien saucer (Godzilla Final Wars). Small, fast and armed with a laser, and each one carries a Xilien control
 * relay: every monster with M-Base within range fights for the Xiliens while a saucer is overhead. Shoot them down
 * and their monsters are freed.
 */
public class XilienSaucerEntity extends FlyingKaijuEntity {
    public XilienSaucerEntity(EntityType<? extends XilienSaucerEntity> type, Level level) {
        super(type, level);
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(60, 8, 2, 4, 0.3).add(Attributes.FLYING_SPEED, 0.9).add(Attributes.KNOCKBACK_RESISTANCE, 0.3);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("saucer_laser", BeamStyle.VISOR, 1, 0.15f, ModDamageTypes.TOWER_LASER, 10, 20, 80)
                .damage(4f, 4).blocks(0.6f, true).tracking(0.25f, 0f).range(2, 40).chance(0.08f));
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 0.1, 1.0);
    }

    @Override
    public boolean alwaysFlies() {
        return true;
    }

    @Override
    public boolean canFlyNow() {
        return true;
    }

    @Override
    protected float regenPerSecond() {
        return 0f;
    }

    @Override
    public boolean showsBossBar() {
        return false;
    }

    @Override
    protected boolean reactsToEmp() {
        return true;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (tickCount % 20 == 0 && level() instanceof ServerLevel server && !isEmpDisabled()) {
            AlienControl.broadcast(server, position(), GodzillaConfig.get(GodzillaConfig.SAUCER_CONTROL_RANGE, 96), AlienSignal.XILIEN);
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY() - 0.4, getZ(), 4, 0.6, 0.1, 0.6, 0.05);
        }
    }

    @Override
    public void playWingSound() {}

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.XILIEN_SAUCER.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_EXPLODE.value();
    }

    @Override
    protected float getSoundVolume() {
        return 2.0f;
    }
}

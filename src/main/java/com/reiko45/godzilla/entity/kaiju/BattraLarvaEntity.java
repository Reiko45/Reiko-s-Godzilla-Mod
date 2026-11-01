package com.reiko45.godzilla.entity.kaiju;

import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.LarvaKaijuEntity;
import com.reiko45.godzilla.entity.ability.BeamAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.RoarAbility;
import com.reiko45.godzilla.entity.ability.RollAbility;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Battra larva (1992): Mothra's dark twin, an armoured, horned larva that tears through cities. Fires prism beams from
 * its eyes and rams with its horn. Matures into the adult Battra.
 */
public class BattraLarvaEntity extends LarvaKaijuEntity {
    public BattraLarvaEntity(EntityType<? extends BattraLarvaEntity> type, Level level) {
        super(type, level);
        this.xpReward = 300;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(550, 16, 6, 20, 0.2);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    protected EntityType<? extends FlyingKaijuEntity> adultType() {
        return ModEntities.BATTRA.get();
    }

    @Override
    protected String emergeMessage() {
        return "message.reikogodzilla.battra_emerges";
    }

    @Override
    protected void onEmerge(FlyingKaijuEntity adult) {
        adult.playSound(ModSounds.BATTRA_CRY.get(), 8f, 1f);
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("prism_beam", BeamStyle.PRISM, 1, 0.45f, ModDamageTypes.PRISM_BEAM, 20, 40, 180)
                .damage(9f, 4).blocks(1.4f, false).tracking(0.15f, 0f).chargeParticle(net.minecraft.core.particles.ParticleTypes.WITCH)
                .range(4, 40));
        list.add(new RollAbility("horn_ram", "charge", "ram", 1.2));
        list.add(new RoarAbility(ModSounds.BATTRA_CRY));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "body", 3.4f, 2.6f, new Vec3(0, 0, -4.0), 0.9f),
                new KaijuPart(this, "tail", 2.6f, 2.0f, new Vec3(0, 0, -7.4), 0.8f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 2.4, 2.4);
    }

    @Override
    protected double baseStepHeight() {
        return 2.0;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isCocoon() ? null : ModSounds.BATTRA_CRY.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SILVERFISH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SILVERFISH_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 0.8f;
    }
}

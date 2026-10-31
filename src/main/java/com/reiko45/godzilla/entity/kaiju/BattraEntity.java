package com.reiko45.godzilla.entity.kaiju;

import com.reiko45.godzilla.entity.*;
import com.reiko45.godzilla.entity.ability.BeamAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.WindGustAbility;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Battra (adult, 1992). Fires prism beams from its eyes and electric bolts from its horn. Starts as an enemy of Mothra
 * and Godzilla, but once badly hurt with Mothra nearby it changes sides and fights beside her.
 */
public class BattraEntity extends FlyingKaijuEntity {
    private static final EntityDataAccessor<Boolean> DATA_ALLIED = SynchedEntityData.defineId(BattraEntity.class, EntityDataSerializers.BOOLEAN);

    public BattraEntity(EntityType<? extends BattraEntity> type, Level level) {
        super(type, level);
        this.xpReward = 450;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(650, 16, 6, 20, 0.2).add(Attributes.FLYING_SPEED, 0.7);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ALLIED, false);
    }

    public boolean isAllied() {
        return entityData.get(DATA_ALLIED);
    }

    @Override
    public KaijuFaction getFaction() {
        return entityData != null && isAllied() ? KaijuFaction.GUARDIAN : KaijuFaction.INVADER;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(3, new com.reiko45.godzilla.entity.ai.GuardianDefendGoal(this) {
            @Override
            public boolean canUse() {
                return isAllied() && super.canUse();
            }
        });
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("prism_beam", BeamStyle.PRISM, 2, 0.4f, ModDamageTypes.PRISM_BEAM, 15, 45, 180)
                .damage(10f, 4).blocks(1.5f, false).tracking(0.18f, 0f).chargeParticle(ParticleTypes.WITCH)
                .range(4, 48));
        list.add(new BeamAbility("horn_bolts", BeamStyle.ELECTRIC, 1, 0.35f, ModDamageTypes.PRISM_BEAM, 10, 30, 160)
                .damage(8f, 3).blocks(1.0f, true).tracking(0.3f, 2f).chargeParticle(ParticleTypes.ELECTRIC_SPARK)
                .range(4, 40));
        list.add(new WindGustAbility(2.5f));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "left_wing", 7.5f, 1.0f, new Vec3(-6.5, 1.8, 0), 0.6f),
                new KaijuPart(this, "right_wing", 7.5f, 1.0f, new Vec3(6.5, 1.8, 0), 0.6f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return index == 0 ? new Vec3(-0.5, 2.6, 2.2) : index == 1 ? new Vec3(0.5, 2.6, 2.2) : new Vec3(0, 3.2, 2.6);
    }

    @Override
    public int landChance() {
        return 900;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!isAllied() && tickCount % 20 == 0 && getHealth() < getMaxHealth() * 0.35f && level() instanceof ServerLevel server
                && !server.getEntitiesOfClass(MothraEntity.class, getBoundingBox().inflate(64)).isEmpty()) {
            becomeAlly(server);
        }
    }

    /** Mothra persuades Battra to stand with her (Godzilla vs. Mothra, 1992). */
    private void becomeAlly(ServerLevel server) {
        entityData.set(DATA_ALLIED, true);
        setTarget(null);
        heal(getMaxHealth() * 0.25f);
        for (KaijuEntity k : server.getEntitiesOfClass(KaijuEntity.class, getBoundingBox().inflate(96), k -> k.getTarget() == this)) {
            if (!k.wantsToAttack(this)) k.setTarget(null);
        }
        server.sendParticles(ParticleTypes.END_ROD, getX(), getY(0.5), getZ(), 80, getBbWidth(), getBbHeight(), getBbWidth(), 0.1);
        for (var p : server.players()) {
            if (p.distanceToSqr(this) < 160 * 160) p.sendSystemMessage(Component.translatable("message.reikogodzilla.battra_ally").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    @Override
    public void tick() {
        super.tick();
        // Boss bar colour follows its allegiance.
        if (!level().isClientSide && tickCount % 20 == 0) refreshBossColor();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Allied", isAllied());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_ALLIED, tag.getBoolean("Allied"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.BATTRA_CRY.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.BATTRA_CRY.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.BATTRA_CRY.get();
    }
}

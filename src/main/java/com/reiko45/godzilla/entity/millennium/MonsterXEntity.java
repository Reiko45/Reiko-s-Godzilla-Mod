package com.reiko45.godzilla.entity.millennium;

import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Monster X (Godzilla Final Wars, 2004), the Xiliens' trump card from the Gorath asteroid. A skeletal fighter who
 * levitates, moves like a martial artist and fires "Destroyed Thunder" gravity beams from the eyes of his skull and his
 * shoulder skulls.
 * <p>
 * Beaten down to half health he sheds his exoskeleton and becomes Keizer Ghidorah: a three-headed quadruped whose
 * anti-gravity beams lift and hurl even Godzilla, whose bites drain his enemies' energy, and who can raise a barrier.
 */
public class MonsterXEntity extends KaijuEntity {
    public static final int MONSTER_X = 0;
    public static final int KEIZER = 1;
    private static final int TRANSFORM_TICKS = 60;
    private int transforming;

    public MonsterXEntity(EntityType<? extends MonsterXEntity> type, Level level) {
        super(type, level);
        this.xpReward = 800;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(1300, 24, 12, 32, 0.28);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    public boolean hasMBase() {
        return true;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("destroyed_thunder", BeamStyle.XILIEN_GRAVITY, 3, 0.4f, ModDamageTypes.DESTROYED_THUNDER, 20, 45, 180)
                .damage(9f, 4).blocks(1.6f, false).lift(0.3f).multiTarget().chargeParticle(ParticleTypes.WITCH)
                .range(4, 56).when(k -> k.getVariant() == MONSTER_X));
        list.add(new LeapAbility("levitate", 1.6, 1.4f, 200).when(k -> k.getVariant() == MONSTER_X));
        list.add(new BeamAbility("destroyed_keizer", BeamStyle.KEIZER, 3, 0.6f, ModDamageTypes.DESTROYED_THUNDER, 25, 60, 200)
                .damage(12f, 4).blocks(2.2f, false).lift(0.8f).multiTarget().chargeParticle(ParticleTypes.FLAME)
                .sounds(ModSounds.GRAVITY_BEAM_CHARGE, ModSounds.GRAVITY_BEAM_FIRE)
                .range(4, 64).when(k -> k.getVariant() == KEIZER));
        list.add(new HoldAbility("draining_bite", "grab", HoldAbility.Mode.MOUTH, 60, 6, 0.45f, 0.7f, 240)
                .damageType(ModDamageTypes.ENERGY_DRAIN).sound(ModSounds.KEIZER_ROAR).when(k -> k.getVariant() == KEIZER));
        list.add(new ForceFieldAbility().when(k -> k.getVariant() == KEIZER));
        list.add(new TailSwipeAbility());
        list.add(new RoarAbility(ModSounds.MONSTER_X_ROAR));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{new KaijuPart(this, "head", 2.0f, 2.0f, new Vec3(0, 8.4, 1.8), 1.4f)};
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return getVariant() == KEIZER ? EntityDimensions.scalable(5.0f, 11.0f).withEyeHeight(11.5f) : super.getDefaultDimensions(pose);
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        if (getVariant() == KEIZER) {
            double[][] heads = {{2.2, 12.2, 4.2}, {0.0, 13.0, 4.8}, {-2.2, 12.2, 4.2}};
            double[] h = heads[Math.min(index, 2)];
            return new Vec3(h[0], h[1], h[2]);
        }
        // Skull eyes, then the shoulder skulls.
        return switch (index) {
            case 1 -> new Vec3(1.6, 7.6, 1.2);
            case 2 -> new Vec3(-1.6, 7.6, 1.2);
            default -> new Vec3(0, 9.2, 2.6);
        };
    }

    @Override
    protected double variantScale() {
        return getVariant() == KEIZER ? 2.15 : 2.5;
    }

    @Override
    protected void applyVariantAttributes() {
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(getVariant() == KEIZER ? 1800 : 1300);
        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(getVariant() == KEIZER ? 38 : 32);
    }

    @Override
    public Component getTypeName() {
        return Component.translatable(getVariant() == KEIZER ? "entity.reikogodzilla.keizer_ghidorah" : "entity.reikogodzilla.monster_x");
    }

    @Override
    public boolean isImmobile() {
        return transforming > 0 || super.isImmobile();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (transforming > 0 && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        if (ForceFieldAbility.isShielding(this) && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            playSound(SoundEvents.SHIELD_BLOCK, 3f, 0.5f);
            return false;
        }
        boolean hurt = super.hurt(source, amount);
        if (hurt && isAlive() && getVariant() == MONSTER_X && transforming == 0 && getHealth() < getMaxHealth() * 0.5f && !level().isClientSide) {
            transforming = TRANSFORM_TICKS;
            if (isAbilityActive()) stopAbility();
            playSound(ModSounds.KEIZER_ROAR.get(), 10f, 0.6f);
        }
        return hurt;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (transforming <= 0 || level().isClientSide) return;
        ServerLevel server = (ServerLevel) level();
        // The exoskeleton cracks and falls away.
        server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()), getX(), getY(0.6), getZ(), 20,
                getBbWidth() * 0.5, getBbHeight() * 0.4, getBbWidth() * 0.5, 0.2);
        server.sendParticles(ParticleTypes.FLAME, getX(), getY(0.5), getZ(), 6, getBbWidth() * 0.5, getBbHeight() * 0.4, getBbWidth() * 0.5, 0.05);
        if (--transforming == 0) {
            setVariant(KEIZER);
            setHealth(getMaxHealth() * 0.7f);
            triggerAnim("action", "roar");
            server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(0.5), getZ(), 2, 1, 1, 1, 0);
            for (var p : server.players()) {
                if (p.distanceToSqr(this) < 256 * 256) p.displayClientMessage(Component.translatable("message.reikogodzilla.keizer"), false);
            }
        }
    }

    @Override
    public void performMelee(LivingEntity target) {
        super.performMelee(target);
        if (getVariant() == MONSTER_X) meleeCooldown = 15;
    }

    @Override
    protected float regenPerSecond() {
        return 1.0f;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Transforming", transforming);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        transforming = tag.getInt("Transforming");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return getVariant() == KEIZER ? ModSounds.KEIZER_ROAR.get() : ModSounds.MONSTER_X_ROAR.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return getVariant() == KEIZER ? ModSounds.KEIZER_ROAR.get() : ModSounds.MONSTER_X_ROAR.get();
    }
}

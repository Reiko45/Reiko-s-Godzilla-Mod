package com.reiko45.godzilla.entity.monsterverse;

import com.reiko45.godzilla.entity.BeamStyle;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.ability.BeamAbility;
import com.reiko45.godzilla.entity.ability.InkCloudAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.RoarAbility;
import com.reiko45.godzilla.entity.ability.TailSwipeAbility;
import com.reiko45.godzilla.entity.ai.KaijuMoveControl;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/**
 * Tiamat, the sea serpent Titan of the Arctic (Godzilla x Kong: The New Empire). Fast and deadly in water, clumsy and
 * vulnerable on land. Blinding phosphorus ink, electric coils, razor-edged scales that cut anything that hits her.
 * She guards the solar radiation stored in her lair. When she dies, Godzilla nearby absorbs it and evolves.
 */
public class TiamatEntity extends KaijuEntity {
    public TiamatEntity(EntityType<? extends TiamatEntity> type, Level level) {
        super(type, level);
        this.xpReward = 600;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(1000, 16, 8, 26, 0.3);
    }

    @Override
    public KaijuFaction getFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("electric_coils", BeamStyle.LIGHTNING, 1, 0.3f, ModDamageTypes.ELECTRIC_COIL, 10, 30, 120)
                .damage(10f, 3).blocks(0.5f, false).tracking(0.5f, 1.5f).chargeParticle(ParticleTypes.ELECTRIC_SPARK)
                .range(2, 18));
        list.add(new InkCloudAbility());
        list.add(new TailSwipeAbility());
        list.add(new RoarAbility(ModSounds.TIAMAT_HISS));
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "head", 2.2f, 2.0f, new Vec3(0, 2.0, 6.0), 1.4f),
                new KaijuPart(this, "coil1", 3.0f, 2.6f, new Vec3(0, 1.2, -5.0), 0.8f),
                new KaijuPart(this, "coil2", 2.6f, 2.2f, new Vec3(0, 1.0, -9.5), 0.7f),
                new KaijuPart(this, "tail", 2.0f, 1.8f, new Vec3(0, 0.8, -13.5), 0.6f)
        };
    }

    @Override
    protected Vec3 beamLocalOffset(int index) {
        return new Vec3(0, 2.2, 7.0);
    }

    @Override
    protected double variantScale() {
        return 1.7;
    }

    @Override
    protected MoveControl createMoveControl() {
        return new SwimMoveControl(this);
    }

    /** Swims freely in three dimensions in water; drags herself slowly over land. */
    private static class SwimMoveControl extends KaijuMoveControl {
        SwimMoveControl(Mob mob) {
            super(mob);
        }

        @Override
        public void tick() {
            if (!mob.isInWater()) {
                mob.setNoGravity(false);
                super.tick();
                mob.setSpeed(mob.getSpeed() * 0.4f);
                return;
            }
            mob.setNoGravity(true);
            mob.setSpeed(0);
            mob.setZza(0);
            if (operation != Operation.MOVE_TO) {
                mob.setDeltaMovement(mob.getDeltaMovement().scale(0.9));
                return;
            }
            operation = Operation.WAIT;
            Vec3 to = new Vec3(wantedX - mob.getX(), wantedY - mob.getY(), wantedZ - mob.getZ());
            if (to.lengthSqr() < 1.0) {
                mob.setDeltaMovement(mob.getDeltaMovement().scale(0.8));
                return;
            }
            float targetYaw = (float) (Mth.atan2(to.z, to.x) * (180.0 / Math.PI)) - 90.0f;
            mob.setYRot(rotlerp(mob.getYRot(), targetYaw, 10f));
            mob.yBodyRot = mob.getYRot();
            double speed = speedModifier * mob.getAttributeValue(Attributes.MOVEMENT_SPEED) * 3.0 * Math.max(1.0, Math.sqrt(mob.getScale()));
            mob.setDeltaMovement(mob.getDeltaMovement().lerp(to.normalize().scale(speed), 0.15));
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Out of the water she's at a severe disadvantage.
        return super.hurt(source, isInWater() ? amount : amount * 1.5f);
    }

    /** Razor-edged scales. */
    @Override
    protected void actuallyHurt(DamageSource source, float amount) {
        super.actuallyHurt(source, amount);
        if (source.getDirectEntity() instanceof LivingEntity attacker && source.getDirectEntity() == source.getEntity()
                && attacker.distanceToSqr(this) < Math.pow(getBbWidth() * 2 + 4, 2)) {
            attacker.hurt(damageSources().thorns(this), Math.min(12f, amount * 0.2f));
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!isInWater() && isNoGravity()) setNoGravity(false);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!(level() instanceof ServerLevel server)) return;
        // Godzilla x Kong: The New Empire - Godzilla takes the energy of her lair and emerges Evolved.
        server.getEntitiesOfClass(MonsterverseGodzillaEntity.class, getBoundingBox().inflate(128), KaijuEntity::isAlive).stream()
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .ifPresentOrElse(MonsterverseGodzillaEntity::evolve, () -> {
                    for (var p : server.players()) {
                        if (p.distanceToSqr(this) < 160 * 160) p.sendSystemMessage(Component.translatable("message.reikogodzilla.tiamat_falls").withStyle(ChatFormatting.LIGHT_PURPLE));
                    }
                });
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.TIAMAT_HISS.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.TIAMAT_HISS.get();
    }

    private static final software.bernie.geckolib.animation.RawAnimation SWIM = software.bernie.geckolib.animation.RawAnimation.begin().thenLoop("swim");

    @Override
    protected software.bernie.geckolib.animation.RawAnimation movementAnimation(software.bernie.geckolib.animation.AnimationState<KaijuEntity> state) {
        return isInWater() ? SWIM : super.movementAnimation(state);
    }
}

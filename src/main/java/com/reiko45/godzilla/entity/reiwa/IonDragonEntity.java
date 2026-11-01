package com.reiko45.godzilla.entity.reiwa;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.*;
import com.reiko45.godzilla.entity.ability.*;
import com.reiko45.godzilla.registry.ModDamageTypes;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.registry.ModItems;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.util.KaijuDestruction;
import com.reiko45.godzilla.world.InstantBeam;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Ion Dragon (Monarch: Legacy of Monsters): a dragon of Philippine legend with an anglerfish-like mouth. It leaves
 * ionizing trails through the air, breathes ionized energy and spits nacre. Its wings are its weak point: tear one and
 * it can't fly any more (Godzilla tore off a wing and threw it into the rift).
 */
public class IonDragonEntity extends FlyingKaijuEntity {
    private float wingDamage;
    private boolean grounded;

    public IonDragonEntity(EntityType<? extends IonDragonEntity> type, Level level) {
        super(type, level);
        this.xpReward = 400;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(420, 10, 6, 20, 0.24).add(Attributes.FLYING_SPEED, 0.8);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("ion_breath", BeamStyle.ION, 1, 0.45f, ModDamageTypes.ION_TRAIL, 20, 40, 220)
                .damage(10f, 4).blocks(1.0f, false).chargeParticle(ParticleTypes.ELECTRIC_SPARK)
                .terrain((level, k, hit, radius) -> {
                    if (k.tickCount % 10 == 0) ((IonDragonEntity) k).ionCloud(level, hit, 3f, 200);
                }).range(4, 40));
        list.add(new SprayAbility("nacre_spit", k -> new com.reiko45.godzilla.entity.projectile.AcidSapEntity(k.level(), k), ModSounds.ION_DRAGON_SCREECH, 1.2f, 4f, 20, 200)
                .interval(5));
        list.add(new DiveAbility(2.0f));
        list.add(new RoarAbility(ModSounds.ION_DRAGON_SCREECH));
    }

    private void ionCloud(ServerLevel level, Vec3 at, float radius, int duration) {
        var cloud = new net.minecraft.world.entity.AreaEffectCloud(level, at.x, at.y, at.z);
        cloud.setRadius(radius);
        cloud.setDuration(duration);
        cloud.setRadiusPerTick(-radius / duration);
        cloud.setParticle(ParticleTypes.ELECTRIC_SPARK);
        cloud.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
        cloud.setOwner(this);
        level.addFreshEntity(cloud);
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "wing_left", 2.4f, 0.8f, new Vec3(2.6, 2.2, -0.4), 1.2f),
                new KaijuPart(this, "wing_right", 2.4f, 0.8f, new Vec3(-2.6, 2.2, -0.4), 1.2f)
        };
    }

    @Override
    protected double variantScale() {
        return 2.2;
    }

    @Override
    public boolean hurtFromPart(KaijuPart part, DamageSource source, float amount) {
        if (part.name.startsWith("wing") && !grounded) {
            wingDamage += amount * part.damageMultiplier;
            if (wingDamage > getMaxHealth() * 0.15f) {
                grounded = true;
                setFlying(false);
                playSound(SoundEvents.PHANTOM_DEATH, 4f, 0.5f);
            }
        }
        return super.hurtFromPart(part, source, amount);
    }

    @Override
    public boolean canFlyNow() {
        return !grounded;
    }

    @Override
    public boolean prefersFlightInCombat() {
        return !grounded;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (level() instanceof ServerLevel server && isFlying() && tickCount % 15 == 0) ionCloud(server, position().add(0, 1, 0), 2f, 160);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Grounded", grounded);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        grounded = tag.getBoolean("Grounded");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ION_DRAGON_SCREECH.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ION_DRAGON_SCREECH.get();
    }
}

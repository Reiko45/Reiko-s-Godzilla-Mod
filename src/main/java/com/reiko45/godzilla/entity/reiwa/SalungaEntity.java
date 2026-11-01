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
 * Salunga (Godzilla Singular Point): the kaiju imprisoned over the Red Dust pool in India's SHIVA facility. It hits
 * with electrified punches that short out machines, and pulls the Red Dust around it into itself to heal.
 */
public class SalungaEntity extends KaijuEntity {
    public SalungaEntity(EntityType<? extends SalungaEntity> type, Level level) {
        super(type, level);
        this.xpReward = 600;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(620, 16, 8, 26, 0.24);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new GrabThrowAbility());
        list.add(new LeapAbility("pounce", 1.2, 1.4f, 240));
        list.add(new RoarAbility(ModSounds.SALUNGA_ROAR));
    }

    @Override
    public void performMelee(LivingEntity target) {
        super.performMelee(target);
        if (level() instanceof ServerLevel server) {
            KaijuDestruction.hurtIgnoringCooldown(target, ModDamageTypes.source(server, ModDamageTypes.SHOCK_PUNCH, this), 8f);
            if (target instanceof EmpSensitive emp) emp.applyEmp(60);
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY(0.6), target.getZ(), 20, 0.6, 0.6, 0.6, 0.3);
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel server) || tickCount % 40 != 0 || getHealth() >= getMaxHealth()) return;
        int eaten = 0;
        BlockPos c = blockPosition();
        for (int i = 0; i < 40 && eaten < 6; i++) {
            BlockPos p = c.offset(random.nextInt(17) - 8, random.nextInt(5) - 2, random.nextInt(17) - 8);
            if (server.getBlockState(p).getBlock() instanceof com.reiko45.godzilla.block.RedDustBlock) {
                server.removeBlock(p, false);
                Vec3 from = Vec3.atCenterOf(p);
                server.sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.85f, 0.15f, 0.1f), 1.5f),
                        from.x, from.y, from.z, 6, 0.3, 0.3, 0.3, 0);
                eaten++;
            }
        }
        if (eaten > 0) heal(eaten * 12f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.SALUNGA_ROAR.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SALUNGA_ROAR.get();
    }
}

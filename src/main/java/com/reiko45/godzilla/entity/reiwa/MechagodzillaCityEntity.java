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
 * The control core of Mechagodzilla City (GODZILLA: City on the Edge of Battle): Mechagodzilla's surviving head, whose
 * nanometal grew over twenty thousand years into a fortress. It never moves. While it lives, nanometal around it is
 * active and keeps spreading. It fights Godzilla with nanometal spikes, EMP harpoons, rail guns, a particle cloud that
 * deflects beams and liquid nanometal that pins kaiju in place. If a Vulture pilot is fully assimilated, the City
 * turns on everyone. Destroy the core and all of its nanometal dies with it.
 */
public class MechagodzillaCityEntity extends KaijuEntity {
    private static final EntityDataAccessor<Boolean> DATA_HOSTILE = SynchedEntityData.defineId(MechagodzillaCityEntity.class, EntityDataSerializers.BOOLEAN);

    public MechagodzillaCityEntity(EntityType<? extends MechagodzillaCityEntity> type, Level level) {
        super(type, level);
        this.xpReward = 1500;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(2400, 30, 20, 20, 0.0).add(Attributes.FOLLOW_RANGE, 128);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return isHostile() ? KaijuFaction.INVADER : KaijuFaction.HUMAN;
    }

    public boolean isHostile() {
        return entityData.get(DATA_HOSTILE);
    }

    public void turnHostile() {
        if (isHostile()) return;
        entityData.set(DATA_HOSTILE, true);
        refreshBossColor();
        if (level() instanceof ServerLevel server) {
            for (ServerPlayer p : server.players()) {
                if (p.distanceToSqr(this) < 256 * 256) p.sendSystemMessage(Component.translatable("message.reikogodzilla.city_hostile").withStyle(ChatFormatting.DARK_AQUA));
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_HOSTILE, false);
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new BeamAbility("rail_guns", BeamStyle.NANOMETAL, 2, 0.35f, ModDamageTypes.NANOMETAL, 15, 30, 140)
                .damage(12f, 3).blocks(1.0f, false).range(4, 96));
        list.add(new MissileBarrageAbility("emp_harpoons", new Vec3(-3, 6, 3), new Vec3(3, 6, 3),
                com.reiko45.godzilla.entity.projectile.KaijuMissileEntity.Payload.PARALYZER, 220).interval(6).range(6, 80));
        list.add(new ReflectShieldAbility("nanometal_cloud", ParticleTypes.WHITE_ASH));
        list.add(new KaijuAbility("nanometal_spikes", 20, 10, 160, "charge", "pulse") {
            @Override
            public void activate(KaijuEntity k, @Nullable LivingEntity target) {
                if (target != null && k.level() instanceof ServerLevel server) ((MechagodzillaCityEntity) k).spikes(server, target);
            }
        }.range(2, 64));
        list.add(new KaijuAbility("liquid_nanometal", 30, 10, 600, "charge", "pulse") {
            @Override
            public void activate(KaijuEntity k, @Nullable LivingEntity target) {
                if (target != null && k.level() instanceof ServerLevel server) ((MechagodzillaCityEntity) k).trap(server, target);
            }
        }.range(2, 64).when(k -> k.getTarget() instanceof KaijuEntity));
    }

    /** Nanometal spears burst out of the ground under the target. */
    private void spikes(ServerLevel server, LivingEntity target) {
        Vec3 c = target.position();
        boolean grief = KaijuDestruction.canGrief(server, this);
        var nano = com.reiko45.godzilla.registry.ModBlocks.NANOMETAL.get().defaultBlockState().setValue(com.reiko45.godzilla.block.NanometalBlock.ACTIVE, true);
        for (int i = 0; i < 8; i++) {
            double a = Math.PI * 2 * i / 8;
            double r = 2 + target.getBbWidth() * 0.6;
            BlockPos base = BlockPos.containing(c.x + Math.cos(a) * r, c.y, c.z + Math.sin(a) * r);
            server.sendParticles(ParticleTypes.CRIT, base.getX() + 0.5, base.getY() + 1, base.getZ() + 0.5, 12, 0.2, 1.5, 0.2, 0.3);
            if (grief) for (int h = 0; h < 3 + random.nextInt(3); h++) {
                BlockPos p = base.above(h);
                if (server.getBlockState(p).canBeReplaced()) server.setBlock(p, nano, 3);
            }
        }
        for (LivingEntity e : server.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(4), e -> e != this && wantsToAttack(e) || e == target)) {
            KaijuDestruction.hurtIgnoringCooldown(e, ModDamageTypes.source(server, ModDamageTypes.NANOMETAL, this), e instanceof KaijuEntity ? 24f : 10f);
            if (e instanceof KaijuEntity k) k.stun(20);
        }
        server.playSound(null, c.x, c.y, c.z, SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 4f, 0.5f);
    }

    /** A flood of liquid nanometal that hardens around the target's legs. */
    private void trap(ServerLevel server, LivingEntity target) {
        var liquid = com.reiko45.godzilla.registry.ModBlocks.LIQUID_NANOMETAL.get().defaultBlockState();
        int r = (int) Math.ceil(target.getBbWidth() / 2) + 1;
        BlockPos c = target.blockPosition();
        for (int dx = -r; dx <= r; dx++)
            for (int dz = -r; dz <= r; dz++)
                for (int dy = 0; dy <= 1; dy++) {
                    BlockPos p = c.offset(dx, dy, dz);
                    if (server.getBlockState(p).isAir()) server.setBlock(p, liquid, 3);
                }
        if (target instanceof KaijuEntity k) k.stun(100);
        server.playSound(null, c, SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.HOSTILE, 4f, 0.6f);
    }

    @Override
    public boolean wantsToAttack(LivingEntity other) {
        if (!isHostile() && other instanceof KaijuEntity k && k.isGodzilla() && k.isAlive()) return true;
        return super.wantsToAttack(other);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel server)) return;
        setDeltaMovement(0, Math.min(0, getDeltaMovement().y), 0);
        if (tickCount % 100 == 0) com.reiko45.godzilla.world.NanometalCores.checkIn(server, getUUID(), blockPosition());
        if (tickCount % 20 == 0 && KaijuDestruction.canGrief(server, this)) {
            var nano = com.reiko45.godzilla.registry.ModBlocks.NANOMETAL.get().defaultBlockState().setValue(com.reiko45.godzilla.block.NanometalBlock.ACTIVE, true);
            int seeds = isHostile() ? 6 : 2, r = 16;
            for (int i = 0; i < seeds * 4; i++) {
                BlockPos p = blockPosition().offset(random.nextInt(r * 2 + 1) - r, random.nextInt(r) - r / 2, random.nextInt(r * 2 + 1) - r);
                if (com.reiko45.godzilla.block.NanometalBlock.absorbs(server.getBlockState(p))) {
                    server.setBlock(p, nano, 3);
                    if (--seeds <= 0) break;
                }
            }
        }
    }

    @Override
    public boolean canWander() {
        return false;
    }

    @Override
    public boolean canCrushBlocks() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (reason.shouldDestroy()) com.reiko45.godzilla.world.NanometalCores.remove(getUUID());
        super.remove(reason);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Hostile", isHostile());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_HOSTILE, tag.getBoolean("Hostile"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.NANOMETAL_HUM.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }
}

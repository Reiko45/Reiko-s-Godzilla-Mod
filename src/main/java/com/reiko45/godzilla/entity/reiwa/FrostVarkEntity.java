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
 * The Frost Vark (Monarch: Legacy of Monsters): a mole-like "thermovore" from remote Alaska. It burrows, and it feeds
 * by inhaling heat: water freezes, fires and furnaces go out, and anything in front of it is frozen stiff. Fire,
 * lava and explosives hurt it badly (Outpost 88's self-destruct killed one).
 */
public class FrostVarkEntity extends KaijuEntity {
    public FrostVarkEntity(EntityType<? extends FrostVarkEntity> type, Level level) {
        super(type, level);
        this.xpReward = 400;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(450, 14, 6, 22, 0.24);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.INVADER;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new KaijuAbility("heat_inhale", 20, 60, 300, "charge", "beam") {
            @Override
            public void tickActive(KaijuEntity k, @Nullable LivingEntity target, int tick) {
                if (k.level() instanceof ServerLevel server) ((FrostVarkEntity) k).inhale(server, tick);
            }
        }.range(0, 18));
        list.add(new BurrowAbility("burrow", 0.4, 1.5f, 360));
        list.add(new RoarAbility(ModSounds.FROST_VARK_ROAR));
    }

    private void inhale(ServerLevel server, int tick) {
        Vec3 mouth = getEyePosition();
        Vec3 dir = Vec3.directionFromRotation(0, yBodyRot);
        double reach = 20;
        for (int i = 0; i < 8; i++) {
            Vec3 p = mouth.add(dir.scale(random.nextDouble() * reach)).add((random.nextDouble() - 0.5) * 5, (random.nextDouble() - 0.5) * 3, (random.nextDouble() - 0.5) * 5);
            Vec3 v = mouth.subtract(p).normalize().scale(0.6);
            server.sendParticles(ParticleTypes.SNOWFLAKE, p.x, p.y, p.z, 0, v.x, v.y, v.z, 1);
        }
        for (LivingEntity e : server.getEntitiesOfClass(LivingEntity.class, new AABB(mouth, mouth.add(dir.scale(reach))).inflate(4), e -> e != this && !isAlliedTo(e))) {
            if (e instanceof Player p && (p.isCreative() || p.isSpectator())) continue;
            Vec3 to = e.position().subtract(mouth);
            if (to.dot(dir) < 0) continue;
            com.reiko45.godzilla.world.Frost.chill(e, e instanceof KaijuEntity ? 3f : 30f);
            if (tick % 10 == 0) KaijuDestruction.hurtIgnoringCooldown(e, ModDamageTypes.source(server, ModDamageTypes.FROST_INHALE, this), e instanceof KaijuEntity ? 6f : 3f);
        }
        if (tick % 10 == 0) {
            BlockPos at = BlockPos.containing(mouth.add(dir.scale(reach * 0.6)));
            com.reiko45.godzilla.world.Frost.freezeArea(server, at, 6, random, false);
            int drained = 0;
            for (BlockPos p : BlockPos.betweenClosed(at.offset(-5, -3, -5), at.offset(5, 3, 5))) {
                var s = server.getBlockState(p);
                if (s.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT) && s.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT)) {
                    server.setBlock(p, s.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT, false), 3);
                    drained++;
                } else if (s.is(net.minecraft.tags.BlockTags.FIRE)) {
                    server.removeBlock(p, false);
                    drained++;
                }
            }
            if (drained > 0) heal(drained * 6f);
        }
    }

    @Override
    protected double variantScale() {
        return 1.6;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FIRE)) amount *= 2.5f;
        if (source.is(DamageTypeTags.IS_EXPLOSION)) amount *= 1.5f;
        return super.hurt(source, amount);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.FROST_VARK_ROAR.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.FROST_VARK_ROAR.get();
    }
}

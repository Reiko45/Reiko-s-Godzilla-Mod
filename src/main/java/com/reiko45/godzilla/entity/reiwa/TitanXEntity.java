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
 * Titan X, "Co-Cai" (Monarch: Legacy of Monsters, season 2): a huge bioluminescent cephalopod-like sea Titan that
 * migrates through the Pacific. It isn't naturally aggressive, heals quickly in water and lives alongside a swarm of
 * Scarabs. Apex stole its egg and drove it berserk with neural implants: break all three implant nodes on its head to
 * free it. Take its egg and it hunts you; give the egg back to the water beside it and it calms for good.
 */
public class TitanXEntity extends KaijuEntity {
    public static final int WILD = 0, IMPLANTED = 1;
    private final float[] implantDamage = new float[3];
    private boolean[] implantBroken = new boolean[3];
    private int angerTicks;
    @Nullable
    private java.util.UUID thief;
    private boolean blessed;

    public TitanXEntity(EntityType<? extends TitanXEntity> type, Level level) {
        super(type, level);
        this.xpReward = 1000;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(1100, 16, 10, 30, 0.22);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return getVariant() == IMPLANTED || angerTicks > 0 ? KaijuFaction.INVADER : KaijuFaction.TITAN;
    }

    @Override
    protected boolean attacksPlayersOnSight() {
        return getVariant() == IMPLANTED;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new HoldAbility("tentacle_grab", "grab", HoldAbility.Mode.FRONT, 80, 10, 0.5f, 0f, 200).damageType(ModDamageTypes.CONSTRICTION));
        list.add(new KaijuAbility("bioluminescent_pulse", 20, 10, 400, "charge", "pulse") {
            @Override
            public void activate(KaijuEntity k, @Nullable LivingEntity target) {
                if (k.level() instanceof ServerLevel server) ((TitanXEntity) k).pulse(server);
            }
        }.range(0, 32));
        list.add(new TailSwipeAbility());
        list.add(new RoarAbility(ModSounds.TITAN_X_CALL));
    }

    private void pulse(ServerLevel server) {
        double r = 32;
        server.sendParticles(ParticleTypes.GLOW, getX(), getY(0.6), getZ(), 200, r * 0.4, getBbHeight() * 0.5, r * 0.4, 0.2);
        server.sendParticles(ParticleTypes.FLASH, getX(), getY(0.6), getZ(), 2, 0, 0, 0, 0);
        for (LivingEntity e : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(r), e -> e != this)) {
            e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 300, 0));
            if (wantsToAttack(e) || e == getTarget()) {
                e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                KaijuDestruction.hurtIgnoringCooldown(e, ModDamageTypes.source(server, ModDamageTypes.BIOLUMINESCENCE, this), e instanceof KaijuEntity ? 10f : 4f);
            }
        }
    }

    @Override
    protected KaijuPart[] createParts() {
        return new KaijuPart[]{
                new KaijuPart(this, "implant_0", 0.6f, 0.6f, new Vec3(-0.9, 4.6, 1.4), 1.0f),
                new KaijuPart(this, "implant_1", 0.6f, 0.6f, new Vec3(0, 5.0, 1.2), 1.0f),
                new KaijuPart(this, "implant_2", 0.6f, 0.6f, new Vec3(0.9, 4.6, 1.4), 1.0f),
                new KaijuPart(this, "tentacles", 4.0f, 1.6f, new Vec3(0, 0.6, 3.6), 0.8f)
        };
    }

    @Override
    protected double variantScale() {
        return 2.4;
    }

    @Override
    public Component getTypeName() {
        return Component.translatable(getVariant() == IMPLANTED ? "entity.reikogodzilla.titan_x_implanted" : "entity.reikogodzilla.titan_x");
    }

    @Override
    public boolean hurtFromPart(KaijuPart part, DamageSource source, float amount) {
        if (getVariant() == IMPLANTED && part.name.startsWith("implant_")) {
            int i = part.name.charAt(8) - '0';
            if (!implantBroken[i]) {
                implantDamage[i] += amount;
                if (implantDamage[i] > getMaxHealth() * 0.04f) {
                    implantBroken[i] = true;
                    playSound(SoundEvents.GLASS_BREAK, 4f, 0.6f);
                    if (level() instanceof ServerLevel server) server.sendParticles(ParticleTypes.ELECTRIC_SPARK, part.getX(), part.getY(), part.getZ(), 30, 0.4, 0.4, 0.4, 0.3);
                    if (implantBroken[0] && implantBroken[1] && implantBroken[2]) freed();
                }
            }
        }
        return super.hurtFromPart(part, source, amount);
    }

    private void freed() {
        setVariant(WILD);
        refreshBossColor();
        setTarget(null);
        calm(600);
        announce("message.reikogodzilla.titan_x_freed");
    }

    public void eggTaken(Player player) {
        if (blessed) return;
        angerTicks = 6000;
        thief = player.getUUID();
        setTarget(player);
        refreshBossColor();
        announce("message.reikogodzilla.titan_x_egg_taken");
    }

    public void eggReturned(@Nullable Player player) {
        angerTicks = 0;
        thief = null;
        blessed = true;
        setTarget(null);
        refreshBossColor();
        calm(24000);
        if (player != null) {
            player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 12000, 0));
            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 12000, 0));
        }
        announce("message.reikogodzilla.titan_x_calmed");
    }

    private void announce(String key) {
        if (!(level() instanceof ServerLevel server)) return;
        for (ServerPlayer p : server.players()) {
            if (p.distanceToSqr(this) < 192 * 192) p.sendSystemMessage(Component.translatable(key).withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        }
    }

    @Override
    protected float regenPerSecond() {
        return isInWater() ? getMaxHealth() * 0.01f : 0.5f;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel server)) return;
        if (angerTicks > 0) {
            if (--angerTicks == 0) refreshBossColor();
            if (thief != null && tickCount % 20 == 0 && server.getEntity(thief) instanceof Player p && p.isAlive() && !p.isCreative()) setTarget(p);
        }
        if (tickCount % 10 == 0) server.sendParticles(ParticleTypes.GLOW, getX(), getY(0.5), getZ(), 3, getBbWidth() * 0.5, getBbHeight() * 0.4, getBbWidth() * 0.5, 0);
    }

    @Override
    public void finalizeSpawnVariant(MobSpawnType type) {
        if (type == MobSpawnType.SPAWN_EGG || type == MobSpawnType.COMMAND) return;
        if (!(level() instanceof ServerLevel server)) return;
        for (int i = 0; i < 6; i++) {
            ScarabEntity scarab = ModEntities.SCARAB.get().create(server);
            if (scarab == null) continue;
            scarab.moveTo(getX() + random.nextInt(9) - 4, getY() + 1, getZ() + random.nextInt(9) - 4, random.nextFloat() * 360, 0);
            server.addFreshEntity(scarab);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Anger", angerTicks);
        tag.putBoolean("Blessed", blessed);
        for (int i = 0; i < 3; i++) tag.putBoolean("Implant" + i, implantBroken[i]);
        if (thief != null) tag.putUUID("Thief", thief);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        angerTicks = tag.getInt("Anger");
        blessed = tag.getBoolean("Blessed");
        for (int i = 0; i < 3; i++) implantBroken[i] = tag.getBoolean("Implant" + i);
        thief = tag.hasUUID("Thief") ? tag.getUUID("Thief") : null;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.TITAN_X_CALL.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KAIJU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.TITAN_X_CALL.get();
    }
}

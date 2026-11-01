package com.reiko45.godzilla.entity.showa;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.TameableKaijuEntity;
import com.reiko45.godzilla.entity.ability.GrabThrowAbility;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.LeapAbility;
import com.reiko45.godzilla.registry.ModSounds;
import com.reiko45.godzilla.world.AlienSignal;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Jet Jaguar (Godzilla vs. Megalon, 1973), the robot built by inventor Goro Ibuki. Deploy one from its item and it's
 * yours: it follows you at human size and grows to giant size to fight alongside you when a hostile kaiju attacks
 * (or when you use the Jet Jaguar Controller). Flying kicks and martial arts.
 * <p>
 * Left alone, far from its owner, it can be hijacked by the Seatopian signal to guide Megalon. Once it has broken free
 * it becomes self-aware and can never be taken over again.
 */
public class JetJaguarEntity extends TameableKaijuEntity {
    public static final int HUMAN = 0;
    public static final int GIANT = 1;
    private int giantTicks;
    private int growCooldown;
    private boolean selfAware;

    public JetJaguarEntity(EntityType<? extends JetJaguarEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(120, 12, 4, 10, 0.32).add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.HUMAN;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new LeapAbility("flying_kick", 1.6, 1.6f, 160));
        list.add(new GrabThrowAbility().when(k -> k.getVariant() == GIANT));
    }

    @Override
    protected double variantScale() {
        return getVariant() == GIANT ? 1.05 : 0.19;
    }

    @Override
    protected void applyVariantAttributes() {
        boolean giant = getVariant() == GIANT;
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(giant ? 700 : 120);
        var attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(giant ? 28 : 10);
        var speed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.setBaseValue(giant ? 0.26 : 0.32);
        if (getHealth() > getMaxHealth()) setHealth(getMaxHealth());
    }

    @Override
    public Component getTypeName() {
        return Component.translatable("entity.reikogodzilla.jet_jaguar");
    }

    public boolean isGiant() {
        return getVariant() == GIANT;
    }

    /** Grows to fight (or shrinks back). */
    public boolean toggleSize(boolean announce) {
        if (level().isClientSide) return false;
        if (isGiant()) {
            setVariant(HUMAN);
            giantTicks = 0;
        } else {
            if (growCooldown > 0) return false;
            float fraction = getHealth() / getMaxHealth();
            setVariant(GIANT);
            setHealth(getMaxHealth() * Math.max(0.5f, fraction));
            giantTicks = GodzillaConfig.get(GodzillaConfig.JET_JAGUAR_GIANT_TICKS, 2400);
            growCooldown = giantTicks + 1200;
        }
        if (isAbilityActive()) stopAbility();
        playSound(SoundEvents.BEACON_POWER_SELECT, 4f, isGiant() ? 0.6f : 1.4f);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY(0.5), getZ(), 60, getBbWidth(), getBbHeight() * 0.5, getBbWidth(), 0.3);
        }
        if (announce && getOwner() instanceof Player owner) {
            owner.displayClientMessage(Component.translatable(isGiant() ? "message.reikogodzilla.jet_jaguar_grow" : "message.reikogodzilla.jet_jaguar_shrink"), true);
        }
        return true;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (growCooldown > 0) growCooldown--;
        if (giantTicks > 0 && --giantTicks <= 0 && isGiant()) toggleSize(true);
        // Grows on its own when a hostile kaiju shows up.
        if (!isGiant() && growCooldown <= 0 && tickCount % 20 == 0 && isTame() && getTarget() instanceof KaijuEntity enemy
                && enemy.getFaction() == KaijuFaction.INVADER) {
            toggleSize(true);
        }
        // Hijacked: guide the nearest Megalon toward its prey, as the Seatopians made it do.
        if (getControllingSignal() == AlienSignal.SEATOPIA && tickCount % 40 == 0) {
            MegalonEntity megalon = level().getEntitiesOfClass(MegalonEntity.class, getBoundingBox().inflate(128)).stream().findFirst().orElse(null);
            if (megalon != null && getTarget() != null && megalon.getTarget() == null) megalon.setTarget(getTarget());
        }
    }

    /** The Seatopians can only hijack it while its owner is far away, and never after it becomes self-aware. */
    @Override
    public boolean controllableBy(AlienSignal signal) {
        return signal == AlienSignal.SEATOPIA && !selfAware;
    }

    @Override
    protected boolean resistsControl() {
        if (selfAware) return true;
        LivingEntity owner = getOwner();
        return owner != null && owner.level() == level() && owner.distanceToSqr(this) < 48 * 48;
    }

    @Override
    protected void onControlLost(AlienSignal signal) {
        selfAware = true;
        if (getOwner() instanceof Player owner) {
            owner.displayClientMessage(Component.translatable("message.reikogodzilla.jet_jaguar_self_aware").withStyle(ChatFormatting.AQUA), false);
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isTame() && isOwnedBy(player)) {
            if (stack.is(Items.IRON_INGOT) && getHealth() < getMaxHealth()) {
                stack.consume(1, player);
                heal(getMaxHealth() * 0.1f);
                playSound(SoundEvents.IRON_GOLEM_REPAIR, 1.5f, 1.2f);
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
            if (stack.isEmpty()) {
                toggleSitting(player);
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
        }
        return super.mobInteract(player, hand);
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
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("GiantTicks", giantTicks);
        tag.putInt("GrowCooldown", growCooldown);
        tag.putBoolean("SelfAware", selfAware);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        giantTicks = tag.getInt("GiantTicks");
        growCooldown = tag.getInt("GrowCooldown");
        selfAware = tag.getBoolean("SelfAware");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.JET_JAGUAR_HUM.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 900;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return isGiant() ? 4f : 1f;
    }
}

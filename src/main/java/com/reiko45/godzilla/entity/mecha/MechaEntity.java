package com.reiko45.godzilla.entity.mecha;

import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Human-built G-Force machines. Climb in (use) to pilot: WASD to move (fly where you look in aircraft), R fires the
 * main weapon, V the special weapon, G transforms or docks. Left alone they defend the area against hostile kaiju.
 * Sneak + use packs one back into its item; an iron block repairs it. Pilots can't be hurt while inside.
 */
public abstract class MechaEntity extends FlyingKaijuEntity {
    protected MechaEntity(EntityType<? extends MechaEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createMechaAttributes(double health, double armor, double attack, double speed, double flySpeed) {
        return createKaijuAttributes(health, armor, 8, attack, speed)
                .add(Attributes.FLYING_SPEED, flySpeed)
                .add(Attributes.FOLLOW_RANGE, 72);
    }

    /** The item this machine packs back into. */
    public abstract Item packedItem();

    @Override
    public KaijuFaction getFaction() {
        return KaijuFaction.HUMAN;
    }

    @Override
    public boolean canBePilotedBy(Player player) {
        return true;
    }

    @Override
    public boolean protectsPilot() {
        return true;
    }

    @Override
    public boolean canFlyNow() {
        return false;
    }

    @Override
    public boolean prefersFlightInCombat() {
        return alwaysFlies();
    }

    @Override
    public boolean canWander() {
        return false;
    }

    @Override
    protected float regenPerSecond() {
        return 0f;
    }

    @Override
    protected float pilotTurnSpeed() {
        return 8f;
    }

    @Override
    protected boolean attacksPlayersOnSight() {
        return false;
    }

    /** Never turns its weapons on players, even if they shoot it. */
    @Override
    public boolean canAttack(LivingEntity target) {
        return (allowsAttackingPlayers() || !(target instanceof Player)) && super.canAttack(target);
    }

    /** Only a machine taken over by something else (Apex Mechagodzilla under Ghidorah's control) turns on people. */
    protected boolean allowsAttackingPlayers() {
        return false;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.IRON_BLOCK) && getHealth() < getMaxHealth()) {
            stack.consume(1, player);
            heal(getMaxHealth() * 0.15f);
            playSound(SoundEvents.IRON_GOLEM_REPAIR, 2f, 0.8f);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (player.isSecondaryUseActive() && !isVehicle()) {
            if (!level().isClientSide) {
                if (!player.getAbilities().instabuild) player.getInventory().placeItemBackInInventory(new ItemStack(packedItem()));
                discard();
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (!isVehicle()) {
            if (!level().isClientSide) {
                player.startRiding(this);
                player.displayClientMessage(Component.translatable("message.reikogodzilla.mecha_controls"), true);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void tickDeath() {
        if (deathTime == 0 && level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(0.5), getZ(), 3, getBbWidth() * 0.5, getBbHeight() * 0.3, getBbWidth() * 0.5, 0);
            playSound(SoundEvents.GENERIC_EXPLODE.value(), 6f, 0.7f);
        }
        super.tickDeath();
    }

    @Override
    public void playWingSound() {
        playSound(SoundEvents.ELYTRA_FLYING, 1.5f, 0.6f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
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
    protected void playStepSound(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        if (!isFlying()) playSound(SoundEvents.IRON_GOLEM_STEP, 1.5f, 0.5f);
    }

    /** Helper for docking / splitting: move a pilot from one machine to another. */
    protected static void transferPilot(Player pilot, Mob to) {
        pilot.stopRiding();
        pilot.startRiding(to, true);
    }
}

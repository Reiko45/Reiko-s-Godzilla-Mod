package com.reiko45.godzilla.entity.monsterverse;

import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.entity.TameableKaijuEntity;
import com.reiko45.godzilla.entity.ability.KaijuAbility;
import com.reiko45.godzilla.entity.ability.LeapAbility;
import com.reiko45.godzilla.entity.ability.SprayAbility;
import com.reiko45.godzilla.entity.projectile.HeavyProjectileEntity;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Suko, the young Great Ape (Godzilla x Kong: The New Empire). Quick, defiant and a good shot with a boulder. He
 * hates the Skar King: he goes for the control crystal first, and his blows shatter it fast. Win him over with fruit.
 */
public class SukoEntity extends TameableKaijuEntity {
    public SukoEntity(EntityType<? extends SukoEntity> type, Level level) {
        super(type, level);
        this.xpReward = 150;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createKaijuAttributes(320, 8, 4, 14, 0.34);
    }

    @Override
    public KaijuFaction getBaseFaction() {
        return KaijuFaction.TITAN;
    }

    @Override
    protected void registerAbilities(List<KaijuAbility> list) {
        list.add(new SprayAbility("boulder_throw", k -> new HeavyProjectileEntity(k.level(), k, new ItemStack(Items.COBBLESTONE), 14f, 1.2f),
                () -> SoundEvents.PLAYER_ATTACK_STRONG, 1.3f, 2f, 20, 140).interval(10).range(6, 32));
        list.add(new LeapAbility("leap", 1.3, 1.3f, 140));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(0, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, SkarKingEntity.class, 10, true, false,
                e -> e instanceof SkarKingEntity skar && skar.hasCrystal()));
    }

    @Override
    protected double variantScale() {
        return 0.9;
    }

    @Override
    public void performMelee(LivingEntity target) {
        super.performMelee(target);
        if (target instanceof SkarKingEntity skar && skar.hasCrystal()) skar.damageCrystal(25f, this);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.MELON_SLICE) || stack.is(Items.APPLE) || stack.is(Items.SWEET_BERRIES) || stack.is(Items.GLOW_BERRIES)) {
            if (!level().isClientSide) {
                stack.consume(1, player);
                heal(30);
                if (!isTame()) {
                    boolean trusted = random.nextInt(4) == 0;
                    if (trusted) {
                        setOwner(player);
                        setTarget(null);
                        player.displayClientMessage(Component.translatable("message.reikogodzilla.suko_trust"), true);
                    }
                    showTamingResult(trusted);
                }
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (isTame() && isOwnedBy(player) && player.isSecondaryUseActive()) {
            toggleSitting(player);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean canCrushBlocks() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.KONG_ROAR.get();
    }

    @Override
    public float getVoicePitch() {
        return 1.3f;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }
}

package com.reiko45.godzilla.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.BiFunction;

/** A thrown item: Light Bombs, Gigan's Bladed Slicer discs. */
public class ThrowableKaijuItem extends Item {
    private final BiFunction<Level, Player, Projectile> factory;
    private final float velocity;
    private final int cooldown;
    private final String tooltip;

    public ThrowableKaijuItem(Properties properties, BiFunction<Level, Player, Projectile> factory, float velocity, int cooldown, String tooltip) {
        super(properties);
        this.factory = factory;
        this.velocity = velocity;
        this.cooldown = cooldown;
        this.tooltip = tooltip;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.6f, 0.5f);
        if (!level.isClientSide) {
            Projectile projectile = factory.apply(level, player);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0f, velocity, 1f);
            level.addFreshEntity(projectile);
        }
        if (cooldown > 0) player.getCooldowns().addCooldown(this, cooldown);
        stack.consume(1, player);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("tooltip.reikogodzilla." + tooltip).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    /** Helper so subclasses and lambdas can find what the thrower is looking at. */
    public static net.minecraft.world.entity.Entity lookTarget(LivingEntity thrower, double range) {
        net.minecraft.world.phys.Vec3 eye = thrower.getEyePosition();
        net.minecraft.world.phys.Vec3 end = eye.add(thrower.getLookAngle().scale(range));
        net.minecraft.world.phys.EntityHitResult hit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(thrower.level(), thrower, eye, end,
                thrower.getBoundingBox().expandTowards(thrower.getLookAngle().scale(range)).inflate(2), e -> e instanceof LivingEntity && e.isPickable() && e != thrower, 0.5f);
        return hit == null ? null : hit.getEntity();
    }
}

package com.reiko45.godzilla.item;

import com.reiko45.godzilla.entity.projectile.OxygenDestroyerEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Throw it into water. Everything alive in the water nearby dies; kaiju are badly hurt and stop regenerating. */
public class OxygenDestroyerItem extends Item {
    public OxygenDestroyerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 0.6f, 0.5f);
        if (!level.isClientSide) {
            OxygenDestroyerEntity entity = new OxygenDestroyerEntity(level, player);
            entity.setItem(stack.copyWithCount(1));
            entity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.0f, 1.0f);
            level.addFreshEntity(entity);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        player.getCooldowns().addCooldown(this, 40);
        stack.consume(1, player);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.reikogodzilla.oxygen_destroyer").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.reikogodzilla.oxygen_destroyer.warning").withStyle(ChatFormatting.RED));
    }
}

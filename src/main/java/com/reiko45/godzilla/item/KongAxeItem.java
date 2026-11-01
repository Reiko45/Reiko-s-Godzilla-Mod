package com.reiko45.godzilla.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Kong's Battle Axe, forged from a dorsal plate of Godzilla's kind (Godzilla vs. Kong). Use it on Kong to arm him;
 * in his hands it drinks in atomic breath. A player can swing it too, if they can lift it.
 */
public class KongAxeItem extends AxeItem {
    public KongAxeItem(Properties properties) {
        super(Tiers.NETHERITE, properties.attributes(AxeItem.createAttributes(Tiers.NETHERITE, 8.0f, -3.3f)));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.reikogodzilla.kong_axe").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}

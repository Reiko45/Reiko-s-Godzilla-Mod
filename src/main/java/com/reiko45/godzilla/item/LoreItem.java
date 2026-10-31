package com.reiko45.godzilla.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** A material with a one-line lore tooltip (tooltip.reikogodzilla.&lt;key&gt;). */
public class LoreItem extends Item {
    private final String key;

    public LoreItem(Properties properties, String key) {
        super(properties);
        this.key = key;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.reikogodzilla." + key).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}

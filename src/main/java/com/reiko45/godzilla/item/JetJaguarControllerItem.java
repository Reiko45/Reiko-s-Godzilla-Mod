package com.reiko45.godzilla.item;

import com.reiko45.godzilla.entity.showa.JetJaguarEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Goro Ibuki's remote: tells your Jet Jaguars within 64 blocks to grow to fighting size (or shrink back). */
public class JetJaguarControllerItem extends Item {
    public JetJaguarControllerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            int count = 0;
            for (JetJaguarEntity robot : level.getEntitiesOfClass(JetJaguarEntity.class, player.getBoundingBox().inflate(64), r -> r.isOwnedBy(player))) {
                if (robot.toggleSize(false)) count++;
            }
            player.displayClientMessage(Component.translatable(count > 0 ? "message.reikogodzilla.jet_jaguar_command" : "message.reikogodzilla.jet_jaguar_none", count)
                    .withStyle(ChatFormatting.AQUA), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.PLAYERS, 1f, 1.6f);
        }
        player.getCooldowns().addCooldown(this, 40);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.reikogodzilla.jet_jaguar_controller").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}

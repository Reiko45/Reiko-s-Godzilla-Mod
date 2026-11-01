package com.reiko45.godzilla.item;

import com.reiko45.godzilla.entity.projectile.KaijuMissileEntity;
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
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * A D-03 drill missile (GMK): fired from the Satsuma submarine into the wound in Godzilla's shoulder. Fire it along
 * your view. Against GMK Godzilla's open wound it does enormous damage.
 */
public class D03MissileItem extends Item {
    public D03MissileItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            KaijuMissileEntity missile = new KaijuMissileEntity(level, player, KaijuMissileEntity.Payload.D03);
            Vec3 eye = player.getEyePosition().add(player.getLookAngle().scale(1.2));
            missile.setPos(eye.x, eye.y - 0.2, eye.z);
            missile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0f, 2.4f, 0.5f);
            level.addFreshEntity(missile);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundSource.PLAYERS, 2f, 0.6f);
        player.getCooldowns().addCooldown(this, 30);
        stack.consume(1, player);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.reikogodzilla.d03_missile").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}

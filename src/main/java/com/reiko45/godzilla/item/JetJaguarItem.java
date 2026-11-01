package com.reiko45.godzilla.item;

import com.reiko45.godzilla.entity.showa.JetJaguarEntity;
import com.reiko45.godzilla.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;

/** Goro Ibuki's robot, folded up for transport. Place it to deploy a Jet Jaguar that's yours. */
public class JetJaguarItem extends Item {
    public JetJaguarItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        JetJaguarEntity robot = ModEntities.JET_JAGUAR.get().create(level);
        if (robot == null) return InteractionResult.FAIL;
        float yaw = context.getPlayer() != null ? context.getPlayer().getYRot() + 180f : 0f;
        robot.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0f);
        robot.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.SPAWN_EGG, null);
        if (context.getPlayer() != null) robot.setOwner(context.getPlayer());
        level.addFreshEntity(robot);
        ItemStack stack = context.getItemInHand();
        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) stack.shrink(1);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.reikogodzilla.jet_jaguar").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.reikogodzilla.jet_jaguar.controls").withStyle(ChatFormatting.DARK_GRAY));
    }
}

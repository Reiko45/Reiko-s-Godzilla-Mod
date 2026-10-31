package com.reiko45.godzilla.item;

import com.reiko45.godzilla.entity.vehicle.MaserTankEntity;
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

/** Deploys a Type 66 Maser Tank on the clicked block. */
public class MaserTankItem extends Item {
    public MaserTankItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        MaserTankEntity tank = ModEntities.MASER_TANK.get().create(level);
        if (tank == null) return InteractionResult.FAIL;
        float yaw = context.getPlayer() != null ? context.getPlayer().getYRot() : 0f;
        tank.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0f);
        tank.yBodyRot = tank.yHeadRot = yaw;
        if (!level.noCollision(tank)) return InteractionResult.FAIL;
        tank.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.SPAWN_EGG, null);
        level.addFreshEntity(tank);
        ItemStack stack = context.getItemInHand();
        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) stack.shrink(1);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.reikogodzilla.maser_tank").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.reikogodzilla.maser_tank.controls").withStyle(ChatFormatting.DARK_GRAY));
    }
}

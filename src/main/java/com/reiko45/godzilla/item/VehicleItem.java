package com.reiko45.godzilla.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;
import java.util.function.Supplier;

/** Deploys a vehicle or mecha on the clicked block (Maser Tank, Garuda, Mechagodzilla, Super X...). */
public class VehicleItem extends Item {
    private final Supplier<? extends EntityType<? extends Mob>> type;
    private final String tooltipKey;

    public VehicleItem(Properties properties, Supplier<? extends EntityType<? extends Mob>> type, String tooltipKey) {
        super(properties);
        this.type = type;
        this.tooltipKey = tooltipKey;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        Mob mob = type.get().create(level);
        if (mob == null) return InteractionResult.FAIL;
        float yaw = context.getPlayer() != null ? context.getPlayer().getYRot() : 0f;
        mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0f);
        mob.yBodyRot = mob.yHeadRot = yaw;
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.SPAWN_EGG, null);
        level.addFreshEntity(mob);
        ItemStack stack = context.getItemInHand();
        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) stack.shrink(1);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.reikogodzilla." + tooltipKey).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.reikogodzilla." + tooltipKey + ".controls").withStyle(ChatFormatting.DARK_GRAY));
    }
}

package com.reiko45.godzilla.item;

import com.reiko45.godzilla.registry.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * The Red Bamboo's yellow juice (Ebirah, Horror of the Deep): bitter stuff brewed from a Letchi Island plant. Drink it
 * and Ebirah keeps away from you and your boat for five minutes. The fake batch smells almost right and does nothing
 * but turn your stomach.
 */
public class YellowJuiceItem extends Item {
    private final boolean fake;

    public YellowJuiceItem(Properties properties, boolean fake) {
        super(properties);
        this.fake = fake;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            if (fake) {
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
            } else {
                entity.addEffect(new MobEffectInstance(ModEffects.REPELLENT, 6000, 0));
            }
            if (entity instanceof ServerPlayer player) player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
        }
        if (entity instanceof Player player && player.getAbilities().instabuild) return stack;
        return ItemUtils.createFilledResult(stack, entity instanceof Player p ? p : null, new ItemStack(Items.GLASS_BOTTLE));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public SoundEvent getDrinkingSound() {
        return SoundEvents.HONEY_DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(fake ? "tooltip.reikogodzilla.fake_yellow_juice" : "tooltip.reikogodzilla.yellow_juice")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}

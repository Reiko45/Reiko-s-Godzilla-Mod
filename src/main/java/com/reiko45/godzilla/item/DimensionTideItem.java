package com.reiko45.godzilla.item;

import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.entity.millennium.MeganulaEntity;
import com.reiko45.godzilla.entity.millennium.MegaguirusEntity;
import com.reiko45.godzilla.entity.projectile.MicroBlackHoleEntity;
import com.reiko45.godzilla.registry.ModEntities;
import com.reiko45.godzilla.util.KaijuDestruction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * The Dimension Tide's targeting uplink (Godzilla vs. Megaguirus). Hold use on a target for three seconds to lock on,
 * then release: the satellite fires a micro black hole at it.
 * <ul>
 *     <li>The target must be under open sky and not in water.</li>
 *     <li>Megaguirus and Meganula jam the lock-on if they're near you or the target.</li>
 *     <li>The satellite needs a long time to recharge between shots.</li>
 * </ul>
 */
public class DimensionTideItem extends Item {
    private static final int LOCK_TICKS = 60;
    private static final double RANGE = 256;

    public DimensionTideItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPYGLASS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
        int used = getUseDuration(stack, user) - remaining;
        if (level.isClientSide || !(user instanceof Player player) || used % 10 != 0) return;
        if (used < LOCK_TICKS) {
            player.displayClientMessage(Component.translatable("message.reikogodzilla.tide_locking", used * 100 / LOCK_TICKS).withStyle(ChatFormatting.YELLOW), true);
        } else if (used == LOCK_TICKS) {
            player.displayClientMessage(Component.translatable("message.reikogodzilla.tide_locked").withStyle(ChatFormatting.GREEN), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 1f, 2f);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remaining) {
        if (!(level instanceof ServerLevel server) || !(user instanceof Player player)) return;
        int used = getUseDuration(stack, user) - remaining;
        if (used < LOCK_TICKS) return;
        Entity target = ThrowableKaijuItem.lookTarget(player, RANGE);
        if (target == null) {
            fail(player, "message.reikogodzilla.tide_no_target");
            return;
        }
        if (target.isInWater() || !server.canSeeSky(target.blockPosition().above((int) target.getBbHeight() + 1))) {
            fail(player, "message.reikogodzilla.tide_no_sky");
            return;
        }
        if (jammed(server, player.getBoundingBox()) || jammed(server, target.getBoundingBox())) {
            fail(player, "message.reikogodzilla.tide_jammed");
            return;
        }
        MicroBlackHoleEntity hole = ModEntities.MICRO_BLACK_HOLE.get().create(server);
        if (hole == null) return;
        hole.moveTo(target.getX(), target.getY(0.5), target.getZ());
        hole.setup((float) (double) GodzillaConfig.get(GodzillaConfig.DIMENSION_TIDE_RADIUS, 8.0), KaijuDestruction.canGrief(server, player));
        server.addFreshEntity(hole);
        player.getCooldowns().addCooldown(this, GodzillaConfig.get(GodzillaConfig.DIMENSION_TIDE_COOLDOWN, 12000));
        player.displayClientMessage(Component.translatable("message.reikogodzilla.tide_fired").withStyle(ChatFormatting.LIGHT_PURPLE), true);
    }

    private static boolean jammed(ServerLevel level, AABB around) {
        AABB area = around.inflate(48);
        return !level.getEntitiesOfClass(MegaguirusEntity.class, area).isEmpty() || !level.getEntitiesOfClass(MeganulaEntity.class, area).isEmpty();
    }

    private static void fail(Player player, String key) {
        player.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.RED), true);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 1f, 0.5f);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.reikogodzilla.dimension_tide").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("tooltip.reikogodzilla.dimension_tide.controls").withStyle(ChatFormatting.DARK_GRAY));
    }
}

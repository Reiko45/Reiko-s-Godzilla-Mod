package com.reiko45.godzilla.item;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuPart;
import com.reiko45.godzilla.entity.showa.TitanosaurusEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Interpol's Supersonic Wave Oscillator (Terror of Mechagodzilla). Points a beam of supersonic waves along your view:
 * Titanosaurus, whose control implant makes him helpless against them, reels stunned. Other kaiju are only staggered.
 */
public class SupersonicOscillatorItem extends Item {
    private static final double RANGE = 64;

    public SupersonicOscillatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.getCooldowns().addCooldown(this, 100);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.PLAYERS, 2f, 2.0f);
        if (level instanceof ServerLevel server) {
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getLookAngle();
            for (int i = 2; i < RANGE; i += 3) {
                Vec3 p = eye.add(look.scale(i));
                server.sendParticles(ParticleTypes.SONIC_BOOM, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            }
            AABB sweep = player.getBoundingBox().expandTowards(look.scale(RANGE)).inflate(4);
            java.util.Set<KaijuEntity> hit = new java.util.HashSet<>();
            for (Entity e : server.getEntities(player, sweep)) {
                KaijuEntity k = e instanceof KaijuEntity ke ? ke : e instanceof KaijuPart part ? part.getParent() : null;
                if (k == null || hit.contains(k)) continue;
                Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
                double along = to.dot(look);
                if (along < 0 || to.subtract(look.scale(along)).length() > e.getBbWidth() * 0.5 + 3) continue;
                hit.add(k);
                if (k instanceof TitanosaurusEntity titanosaurus) {
                    titanosaurus.onSupersonicWave(player);
                    player.displayClientMessage(Component.translatable("message.reikogodzilla.oscillator_hit").withStyle(ChatFormatting.AQUA), true);
                } else {
                    k.stun(15);
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.reikogodzilla.supersonic_oscillator").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}

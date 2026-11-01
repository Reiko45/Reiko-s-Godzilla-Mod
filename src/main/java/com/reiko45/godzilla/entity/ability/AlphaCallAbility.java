package com.reiko45.godzilla.entity.ability;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

/**
 * King Ghidorah's Alpha Call (Godzilla: King of the Monsters): a scream heard around the world. Every Titan that
 * follows the Alpha wakes up and answers it, turning on humanity and on Godzilla.
 */
public class AlphaCallAbility extends KaijuAbility {
    public static final double RANGE = 512;

    public AlphaCallAbility() {
        super("alpha_call", 20, 60, 3600, "roar", "roar");
        this.minRange = 0;
        this.maxRange = 96;
        this.useChance = 0.08f;
    }

    @Override
    public void activate(KaijuEntity kaiju, @Nullable LivingEntity target) {
        if (!(kaiju.level() instanceof ServerLevel level)) return;
        int answered = 0;
        for (KaijuEntity k : level.getEntitiesOfClass(KaijuEntity.class, kaiju.getBoundingBox().inflate(RANGE),
                k -> k != kaiju && k.isAlive() && k.followsAlpha())) {
            k.answerAlphaCall(kaiju);
            answered++;
        }
        for (ServerPlayer p : level.players()) {
            level.playSound(null, p.getX(), p.getY(), p.getZ(), ModSounds.GHIDORAH_CALL.get(), SoundSource.HOSTILE, 1.5f, 0.6f);
            if (p.distanceToSqr(kaiju) < 1024 * 1024) {
                p.sendSystemMessage(Component.translatable(answered > 0 ? "message.reikogodzilla.alpha_call_answered" : "message.reikogodzilla.alpha_call",
                        answered).withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
            }
        }
    }
}

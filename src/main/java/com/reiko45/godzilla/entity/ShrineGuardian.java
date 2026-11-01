package com.reiko45.godzilla.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * The Guardian Monsters of GMK (Baragon, Mothra, King Ghidorah). They wake to fight a GMK Godzilla near their
 * shrine, and turn on anyone who desecrates it.
 */
public interface ShrineGuardian {
    /** Woken at its shrine. {@code vandal} (if any) is the player who broke the shrine: the guardian hunts them. */
    void awaken(@Nullable Player vandal);

    /** A grudge against one player that wears off after a while. */
    final class Vengeance {
        @Nullable
        private UUID target;
        private int ticks;

        public void set(Player player, int ticks) {
            this.target = player.getUUID();
            this.ticks = ticks;
        }

        public boolean isTarget(LivingEntity e) {
            return ticks > 0 && target != null && e.getUUID().equals(target);
        }

        public void tick(KaijuEntity kaiju) {
            if (ticks <= 0) return;
            if (--ticks <= 0) {
                target = null;
                return;
            }
            if (kaiju.getTarget() == null && kaiju.tickCount % 20 == 0 && kaiju.level() instanceof ServerLevel server && target != null
                    && server.getPlayerByUUID(target) instanceof Player p && p.isAlive() && !p.isCreative() && p.distanceToSqr(kaiju) < 96 * 96) {
                kaiju.setTarget(p);
            }
        }

        public void save(CompoundTag tag) {
            if (target != null && ticks > 0) {
                tag.putUUID("Vengeance", target);
                tag.putInt("VengeanceTicks", ticks);
            }
        }

        public void load(CompoundTag tag) {
            target = tag.hasUUID("Vengeance") ? tag.getUUID("Vengeance") : null;
            ticks = tag.getInt("VengeanceTicks");
        }
    }
}

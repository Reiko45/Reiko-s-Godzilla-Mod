package com.reiko45.godzilla.world;

import com.reiko45.godzilla.ReikoGodzilla;
import com.reiko45.godzilla.entity.KaijuEntity;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/** Keeps mech pilots safe inside their cockpits. */
@EventBusSubscriber(modid = ReikoGodzilla.MODID)
public final class PilotEvents {
    private PilotEvents() {}

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player && player.getVehicle() instanceof KaijuEntity mech && mech.protectsPilot()
                && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            event.setCanceled(true);
        }
    }
}

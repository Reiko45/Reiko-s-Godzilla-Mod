package com.reiko45.godzilla.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.reiko45.godzilla.ReikoGodzilla;
import com.reiko45.godzilla.client.model.GodzillaModel;
import com.reiko45.godzilla.client.model.KaijuModel;
import com.reiko45.godzilla.client.model.MechagodzillaModel;
import com.reiko45.godzilla.client.model.MothraLarvaModel;
import com.reiko45.godzilla.client.renderer.KaijuRenderer;
import com.reiko45.godzilla.client.renderer.MaserTankRenderer;
import com.reiko45.godzilla.entity.VehicleWeapon;
import com.reiko45.godzilla.network.FireWeaponPayload;
import com.reiko45.godzilla.registry.ModEntities;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = ReikoGodzilla.MODID, value = Dist.CLIENT)
public final class ClientSetup {
    public static final KeyMapping FIRE_WEAPON = new KeyMapping("key.reikogodzilla.fire_weapon",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.reikogodzilla");

    private ClientSetup() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.GODZILLA.get(), ctx -> new KaijuRenderer<>(ctx, new GodzillaModel(), 3.0f));
        event.registerEntityRenderer(ModEntities.KING_GHIDORAH.get(), ctx -> new KaijuRenderer<>(ctx, new KaijuModel<>("king_ghidorah"), 4.0f));
        event.registerEntityRenderer(ModEntities.MOTHRA_LARVA.get(), ctx -> new KaijuRenderer<>(ctx, new MothraLarvaModel(), 2.0f));
        event.registerEntityRenderer(ModEntities.MOTHRA.get(), ctx -> new KaijuRenderer<>(ctx, new KaijuModel<>("mothra"), 3.0f));
        event.registerEntityRenderer(ModEntities.RODAN.get(), ctx -> new KaijuRenderer<>(ctx, new KaijuModel<>("rodan"), 3.0f));
        event.registerEntityRenderer(ModEntities.ANGUIRUS.get(), ctx -> new KaijuRenderer<>(ctx, new KaijuModel<>("anguirus"), 3.0f));
        event.registerEntityRenderer(ModEntities.MECHAGODZILLA.get(), ctx -> new KaijuRenderer<>(ctx, new MechagodzillaModel(), 3.0f));
        event.registerEntityRenderer(ModEntities.MASER_TANK.get(), MaserTankRenderer::new);

        event.registerEntityRenderer(ModEntities.SILK_SHOT.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.KAIJU_MISSILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.OXYGEN_DESTROYER.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.GHIDORAH_METEOR.get(), ctx -> new ThrownItemRenderer<>(ctx, 6.0f, true));
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(FIRE_WEAPON);
    }

    /** Game-bus client events. */
    @EventBusSubscriber(modid = ReikoGodzilla.MODID, value = Dist.CLIENT)
    public static final class GameEvents {
        private GameEvents() {}

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            while (FIRE_WEAPON.consumeClick()) {
                if (mc.player != null && mc.player.getVehicle() instanceof VehicleWeapon) {
                    PacketDistributor.sendToServer(new FireWeaponPayload());
                }
            }
        }
    }
}

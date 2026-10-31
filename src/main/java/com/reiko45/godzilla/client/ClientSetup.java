package com.reiko45.godzilla.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.reiko45.godzilla.ReikoGodzilla;
import com.reiko45.godzilla.client.model.NamedGeoModel;
import com.reiko45.godzilla.client.renderer.GeoBeamRenderer;
import com.reiko45.godzilla.client.renderer.KaijuRenderer;
import com.reiko45.godzilla.entity.kaiju.*;
import com.reiko45.godzilla.entity.mecha.GForceMechagodzillaEntity;
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
    public static final KeyMapping SPECIAL_WEAPON = new KeyMapping("key.reikogodzilla.special_weapon",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.reikogodzilla");
    public static final KeyMapping TRANSFORM = new KeyMapping("key.reikogodzilla.transform",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.reikogodzilla");

    private ClientSetup() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Phase 1
        event.registerEntityRenderer(ModEntities.GODZILLA.get(), ctx -> new KaijuRenderer<>(ctx,
                new NamedGeoModel<GodzillaEntity>(g -> g.getVariant() == GodzillaEntity.HEISEI ? "godzilla_heisei" : "godzilla_showa"), 3.0f));
        event.registerEntityRenderer(ModEntities.KING_GHIDORAH.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("king_ghidorah"), 4.0f));
        event.registerEntityRenderer(ModEntities.MOTHRA_LARVA.get(), ctx -> new KaijuRenderer<>(ctx,
                new NamedGeoModel<MothraLarvaEntity>(l -> l.isCocoon() ? "mothra_cocoon" : "mothra_larva"), 2.0f));
        event.registerEntityRenderer(ModEntities.MOTHRA.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("mothra"), 3.0f));
        event.registerEntityRenderer(ModEntities.RODAN.get(), ctx -> new KaijuRenderer<>(ctx,
                new NamedGeoModel<RodanEntity>(r -> "rodan", r -> r.getVariant() == RodanEntity.FIRE ? "rodan_fire" : "rodan"), 3.0f));
        event.registerEntityRenderer(ModEntities.ANGUIRUS.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("anguirus"), 3.0f));
        // While disguised, Mechagodzilla wears the Showa Godzilla model.
        event.registerEntityRenderer(ModEntities.MECHAGODZILLA.get(), ctx -> new KaijuRenderer<>(ctx,
                new NamedGeoModel<MechagodzillaEntity>(m -> m.isDisguised() ? "godzilla_showa" : "mechagodzilla"), 3.0f));
        event.registerEntityRenderer(ModEntities.MASER_TANK.get(), ctx -> new GeoBeamRenderer<>(ctx, NamedGeoModel.fixed("maser_tank"), 1.4f));

        // Phase 2: Heisei
        event.registerEntityRenderer(ModEntities.GODZILLA_JUNIOR.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("godzilla_junior"), 1.0f));
        event.registerEntityRenderer(ModEntities.BIOLLANTE.get(), ctx -> new KaijuRenderer<>(ctx,
                new NamedGeoModel<BiollanteEntity>(b -> b.getVariant() == BiollanteEntity.PLANT_BEAST ? "biollante_plant" : "biollante_flower"), 4.0f));
        event.registerEntityRenderer(ModEntities.BATTRA_LARVA.get(), ctx -> new KaijuRenderer<>(ctx,
                new NamedGeoModel<BattraLarvaEntity>(l -> l.isCocoon() ? "mothra_cocoon" : "battra_larva", l -> l.isCocoon() ? "battra_cocoon" : "battra_larva"), 2.5f));
        event.registerEntityRenderer(ModEntities.BATTRA.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("battra"), 3.0f));
        event.registerEntityRenderer(ModEntities.SPACE_GODZILLA.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("space_godzilla"), 4.0f));
        event.registerEntityRenderer(ModEntities.SPACE_CRYSTAL.get(), ctx -> new GeoBeamRenderer<>(ctx, NamedGeoModel.fixed("space_crystal"), 1.5f));
        event.registerEntityRenderer(ModEntities.DESTOROYAH.get(), ctx -> new KaijuRenderer<>(ctx,
                new NamedGeoModel<DestoroyahEntity>(d -> switch (d.getVariant()) {
                    case DestoroyahEntity.AGGREGATE -> "destoroyah_aggregate";
                    case DestoroyahEntity.FLYING -> "destoroyah_flying";
                    default -> "destoroyah_perfect";
                }), 4.0f));
        event.registerEntityRenderer(ModEntities.DESTOROYAH_JUVENILE.get(), ctx -> new GeoBeamRenderer<>(ctx, NamedGeoModel.fixed("destoroyah_juvenile"), 0.8f));
        event.registerEntityRenderer(ModEntities.GFORCE_MECHAGODZILLA.get(), ctx -> new KaijuRenderer<>(ctx,
                new NamedGeoModel<GForceMechagodzillaEntity>(m -> m.isSuper() ? "super_mechagodzilla" : "mechagodzilla_heisei"), 3.0f));
        event.registerEntityRenderer(ModEntities.GARUDA.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("garuda"), 2.0f));
        event.registerEntityRenderer(ModEntities.MOGUERA.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("moguera"), 3.0f));
        event.registerEntityRenderer(ModEntities.STAR_FALCON.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("star_falcon"), 2.0f));
        event.registerEntityRenderer(ModEntities.LAND_MOGUERA.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("land_moguera"), 2.5f));
        event.registerEntityRenderer(ModEntities.SUPER_X.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("super_x"), 2.5f));
        event.registerEntityRenderer(ModEntities.SUPER_X2.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("super_x2"), 2.5f));
        event.registerEntityRenderer(ModEntities.SUPER_X3.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("super_x3"), 3.0f));
        event.registerEntityRenderer(ModEntities.FREEZER_TANK.get(), ctx -> new GeoBeamRenderer<>(ctx, NamedGeoModel.fixed("freezer_tank"), 1.4f));

        // Projectiles
        event.registerEntityRenderer(ModEntities.SILK_SHOT.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.KAIJU_MISSILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.OXYGEN_DESTROYER.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.ACID_SAP.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.GHIDORAH_METEOR.get(), ctx -> new ThrownItemRenderer<>(ctx, 6.0f, true));
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(FIRE_WEAPON);
        event.register(SPECIAL_WEAPON);
        event.register(TRANSFORM);
    }

    /** Game-bus client events. */
    @EventBusSubscriber(modid = ReikoGodzilla.MODID, value = Dist.CLIENT)
    public static final class GameEvents {
        private GameEvents() {}

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            boolean riding = mc.player != null && mc.player.getVehicle() instanceof VehicleWeapon;
            KeyMapping[] keys = {FIRE_WEAPON, SPECIAL_WEAPON, TRANSFORM};
            for (int action = 0; action < keys.length; action++) {
                while (keys[action].consumeClick()) {
                    if (riding) PacketDistributor.sendToServer(new FireWeaponPayload(action));
                }
            }
        }

        /** Pull the third-person camera back far enough to see the kaiju or mech you're riding. */
        @SubscribeEvent
        public static void onCameraDistance(net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.getVehicle() instanceof com.reiko45.godzilla.entity.KaijuEntity kaiju) {
                event.setDistance(Math.max(event.getDistance(), kaiju.getBbHeight() * 1.4f + kaiju.getBbWidth()));
            }
        }

        /** Pilots sit inside their mechs. */
        @SubscribeEvent
        public static void onRenderPlayer(net.neoforged.neoforge.client.event.RenderPlayerEvent.Pre event) {
            if (event.getEntity().getVehicle() instanceof com.reiko45.godzilla.entity.KaijuEntity kaiju && kaiju.hidesPilot()) {
                event.setCanceled(true);
            }
        }
    }
}

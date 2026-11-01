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
                new NamedGeoModel<MechagodzillaEntity>(m -> m.isDisguised() ? "godzilla_showa" : "mechagodzilla",
                        m -> m.isDisguised() ? "godzilla_showa" : m.getVariant() == MechagodzillaEntity.MECHAGODZILLA_2 ? "mechagodzilla_2" : "mechagodzilla"), 3.0f));
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

        // Phase 3: MonsterVerse
        event.registerEntityRenderer(ModEntities.MONSTERVERSE_GODZILLA.get(), ctx -> new KaijuRenderer<>(ctx,
                new NamedGeoModel<com.reiko45.godzilla.entity.monsterverse.MonsterverseGodzillaEntity>(g -> g.isEvolved() ? "godzilla_evolved" : "godzilla_mv"), 4.0f));
        event.registerEntityRenderer(ModEntities.KONG.get(), ctx -> new KaijuRenderer<>(ctx,
                new com.reiko45.godzilla.client.model.ToggleBoneModel<com.reiko45.godzilla.entity.monsterverse.KongEntity>(k -> "kong", k -> "kong",
                        java.util.List.of("axe", "glove"), (k, bone) -> bone.equals("axe") ? k.hasAxe() : k.hasGlove()), 3.0f));
        event.registerEntityRenderer(ModEntities.SUKO.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("suko"), 1.6f));
        event.registerEntityRenderer(ModEntities.GREAT_APE.get(), ctx -> new GeoBeamRenderer<>(ctx, NamedGeoModel.fixed("great_ape"), 0.9f));
        event.registerEntityRenderer(ModEntities.SKAR_KING.get(), ctx -> new KaijuRenderer<>(ctx,
                new com.reiko45.godzilla.client.model.ToggleBoneModel<com.reiko45.godzilla.entity.monsterverse.SkarKingEntity>(k -> "skar_king", k -> "skar_king",
                        java.util.List.of("crystal"), (k, bone) -> k.hasCrystal()), 3.0f));
        event.registerEntityRenderer(ModEntities.SHIMO.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("shimo"), 5.0f));
        event.registerEntityRenderer(ModEntities.MUTO.get(), ctx -> new KaijuRenderer<>(ctx,
                new NamedGeoModel<com.reiko45.godzilla.entity.monsterverse.MutoEntity>(m -> m.getVariant() == com.reiko45.godzilla.entity.monsterverse.MutoEntity.FEMALE ? "muto_female" : "muto_male"), 3.0f));
        event.registerEntityRenderer(ModEntities.SKULLCRAWLER.get(), ctx -> new GeoBeamRenderer<>(ctx, NamedGeoModel.fixed("skullcrawler"), 0.8f));
        event.registerEntityRenderer(ModEntities.SKULL_DEVIL.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("skull_devil"), 2.0f));
        event.registerEntityRenderer(ModEntities.WARBAT.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("warbat"), 2.0f));
        event.registerEntityRenderer(ModEntities.MONSTERVERSE_KING_GHIDORAH.get(), ctx -> new KaijuRenderer<>(ctx,
                new com.reiko45.godzilla.client.model.ToggleBoneModel<com.reiko45.godzilla.entity.monsterverse.MonsterverseGhidorahEntity>(g -> "king_ghidorah_mv", g -> "king_ghidorah_mv",
                        java.util.List.of("head_0", "head_1", "head_2"), (g, bone) -> !g.isHeadSevered(bone.charAt(5) - '0')), 5.0f));
        event.registerEntityRenderer(ModEntities.MONSTERVERSE_RODAN.get(), ctx -> new KaijuRenderer<>(ctx, new NamedGeoModel<>(r -> "rodan", r -> "rodan_mv"), 3.0f));
        event.registerEntityRenderer(ModEntities.MONSTERVERSE_MOTHRA.get(), ctx -> new KaijuRenderer<>(ctx, new NamedGeoModel<>(m -> "mothra", m -> "mothra_mv"), 3.0f));
        event.registerEntityRenderer(ModEntities.SCYLLA.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("scylla"), 4.0f));
        event.registerEntityRenderer(ModEntities.BEHEMOTH.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("behemoth"), 5.0f));
        event.registerEntityRenderer(ModEntities.METHUSELAH.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("methuselah"), 6.0f));
        event.registerEntityRenderer(ModEntities.TIAMAT.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("tiamat"), 3.0f));
        event.registerEntityRenderer(ModEntities.APEX_MECHAGODZILLA.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("apex_mechagodzilla"), 4.0f));
        event.registerEntityRenderer(ModEntities.HEAV.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("heav"), 2.0f));
        event.registerEntityRenderer(ModEntities.HEAVY_PROJECTILE.get(), ctx -> new ThrownItemRenderer<>(ctx, 3.0f, true));

        // Phase 4: Showa and Millennium
        event.registerEntityRenderer(ModEntities.GIGAN.get(), ctx -> new KaijuRenderer<>(ctx,
                new com.reiko45.godzilla.client.model.ToggleBoneModel<com.reiko45.godzilla.entity.showa.GiganEntity>(g -> "gigan",
                        g -> g.getVariant() == com.reiko45.godzilla.entity.showa.GiganEntity.FINAL_WARS ? "gigan_final_wars" : "gigan",
                        java.util.List.of("hooks", "saws"), (g, bone) -> bone.equals("saws") == g.isModified()), 3.0f));
        event.registerEntityRenderer(ModEntities.HEDORAH.get(), ctx -> new KaijuRenderer<>(ctx,
                new NamedGeoModel<com.reiko45.godzilla.entity.showa.HedorahEntity>(h -> switch (h.getVariant()) {
                    case com.reiko45.godzilla.entity.showa.HedorahEntity.AQUATIC -> "hedorah_aquatic";
                    case com.reiko45.godzilla.entity.showa.HedorahEntity.LAND -> "hedorah_land";
                    case com.reiko45.godzilla.entity.showa.HedorahEntity.FLYING -> "hedorah_flying";
                    default -> "hedorah_perfect";
                }, h -> h.getVariant() == com.reiko45.godzilla.entity.showa.HedorahEntity.FINAL_WARS ? "hedorah_final_wars" : switch (h.getVariant()) {
                    case com.reiko45.godzilla.entity.showa.HedorahEntity.AQUATIC -> "hedorah_aquatic";
                    case com.reiko45.godzilla.entity.showa.HedorahEntity.LAND -> "hedorah_land";
                    case com.reiko45.godzilla.entity.showa.HedorahEntity.FLYING -> "hedorah_flying";
                    default -> "hedorah_perfect";
                }), 3.0f));
        event.registerEntityRenderer(ModEntities.MEGALON.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("megalon"), 3.0f));
        event.registerEntityRenderer(ModEntities.JET_JAGUAR.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("jet_jaguar"), 0.6f));
        event.registerEntityRenderer(ModEntities.EBIRAH.get(), ctx -> new KaijuRenderer<>(ctx, variantTexture("ebirah", "ebirah_final_wars"), 3.0f));
        event.registerEntityRenderer(ModEntities.KUMONGA.get(), ctx -> new KaijuRenderer<>(ctx, variantTexture("kumonga", "kumonga_final_wars"), 3.0f));
        event.registerEntityRenderer(ModEntities.KAMACURAS.get(), ctx -> new KaijuRenderer<>(ctx, variantTexture("kamacuras", "kamacuras_final_wars"), 2.0f));
        event.registerEntityRenderer(ModEntities.MINILLA.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("minilla"), 1.0f));
        event.registerEntityRenderer(ModEntities.GOROSAURUS.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("gorosaurus"), 2.5f));
        event.registerEntityRenderer(ModEntities.MANDA.get(), ctx -> new KaijuRenderer<>(ctx, variantTexture("manda", "manda_final_wars"), 3.0f));
        event.registerEntityRenderer(ModEntities.BARAGON.get(), ctx -> new KaijuRenderer<>(ctx, variantTexture("baragon", "baragon_gmk"), 2.5f));
        event.registerEntityRenderer(ModEntities.VARAN.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("varan"), 3.0f));
        event.registerEntityRenderer(ModEntities.KING_CAESAR.get(), ctx -> new KaijuRenderer<>(ctx, variantTexture("king_caesar", "king_caesar_final_wars"), 3.0f));
        event.registerEntityRenderer(ModEntities.TITANOSAURUS.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("titanosaurus"), 3.0f));
        event.registerEntityRenderer(ModEntities.MILLENNIUM_GODZILLA.get(), ctx -> new KaijuRenderer<>(ctx,
                new NamedGeoModel<com.reiko45.godzilla.entity.millennium.MillenniumGodzillaEntity>(g -> switch (g.getVariant()) {
                    case com.reiko45.godzilla.entity.millennium.MillenniumGodzillaEntity.GMK -> "godzilla_gmk";
                    case com.reiko45.godzilla.entity.millennium.MillenniumGodzillaEntity.KIRYU_ERA -> "godzilla_kiryu_era";
                    case com.reiko45.godzilla.entity.millennium.MillenniumGodzillaEntity.FINAL_WARS -> "godzilla_final_wars";
                    default -> "godzilla_millennium";
                }), 4.0f));
        event.registerEntityRenderer(ModEntities.ORGA.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("orga"), 4.0f));
        event.registerEntityRenderer(ModEntities.MILLENNIAN_UFO.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("millennian_ufo"), 4.0f));
        event.registerEntityRenderer(ModEntities.MEGAGUIRUS.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("megaguirus"), 3.0f));
        event.registerEntityRenderer(ModEntities.MEGANULON.get(), ctx -> new GeoBeamRenderer<>(ctx, NamedGeoModel.fixed("meganulon"), 0.6f));
        event.registerEntityRenderer(ModEntities.MEGANULA.get(), ctx -> new GeoBeamRenderer<>(ctx, NamedGeoModel.fixed("meganula"), 0.4f));
        event.registerEntityRenderer(ModEntities.GMK_MOTHRA.get(), ctx -> new KaijuRenderer<>(ctx, new NamedGeoModel<>(m -> "mothra", m -> "mothra_gmk"), 3.0f));
        event.registerEntityRenderer(ModEntities.GMK_KING_GHIDORAH.get(), ctx -> new KaijuRenderer<>(ctx,
                new NamedGeoModel<com.reiko45.godzilla.entity.millennium.GmkGhidorahEntity>(g -> "king_ghidorah_gmk",
                        g -> g.getVariant() == com.reiko45.godzilla.entity.millennium.GmkGhidorahEntity.DRAGON_KING ? "king_ghidorah_dragon_king" : "king_ghidorah_gmk"), 4.0f));
        event.registerEntityRenderer(ModEntities.KIRYU.get(), ctx -> new KaijuRenderer<>(ctx,
                new com.reiko45.godzilla.client.model.ToggleBoneModel<com.reiko45.godzilla.entity.millennium.KiryuEntity>(k -> "kiryu",
                        k -> k.getVariant() == com.reiko45.godzilla.entity.millennium.KiryuEntity.KAI ? "kiryu_kai" : "kiryu",
                        java.util.List.of("zero_cannon", "drill", "triple_maser"),
                        (k, bone) -> bone.equals("zero_cannon") == (k.getVariant() == com.reiko45.godzilla.entity.millennium.KiryuEntity.KIRYU)), 4.0f));
        event.registerEntityRenderer(ModEntities.MONSTER_X.get(), ctx -> new KaijuRenderer<>(ctx,
                new NamedGeoModel<com.reiko45.godzilla.entity.millennium.MonsterXEntity>(m -> m.getVariant() == com.reiko45.godzilla.entity.millennium.MonsterXEntity.KEIZER
                        ? "keizer_ghidorah" : "monster_x"), 5.0f));
        event.registerEntityRenderer(ModEntities.ZILLA.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("zilla"), 3.0f));
        event.registerEntityRenderer(ModEntities.GOTENGO.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("gotengo"), 6.0f));
        event.registerEntityRenderer(ModEntities.XILIEN_SAUCER.get(), ctx -> new KaijuRenderer<>(ctx, NamedGeoModel.fixed("xilien_saucer"), 1.5f));
        event.registerEntityRenderer(ModEntities.WEB_SHOT.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.SLICER_DISC.get(), ctx -> new ThrownItemRenderer<>(ctx, 2.0f, true));
        event.registerEntityRenderer(ModEntities.LIGHT_BOMB.get(), ctx -> new ThrownItemRenderer<>(ctx, 1.0f, true));
        event.registerEntityRenderer(ModEntities.MICRO_BLACK_HOLE.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);

        // Projectiles
        event.registerEntityRenderer(ModEntities.SILK_SHOT.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.KAIJU_MISSILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.OXYGEN_DESTROYER.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.ACID_SAP.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.GHIDORAH_METEOR.get(), ctx -> new ThrownItemRenderer<>(ctx, 6.0f, true));
    }

    /** One model, with a second texture for the entity's other form (variant 1: Final Wars, GMK...). */
    private static <T extends com.reiko45.godzilla.entity.KaijuEntity> NamedGeoModel<T> variantTexture(String model, String variantTexture) {
        return new NamedGeoModel<>(e -> model, e -> e.getVariant() == 1 ? variantTexture : model);
    }

    /** Vanilla hides the crosshair in third person; pilots need it to aim their weapons. */
    @SubscribeEvent
    public static void registerGuiLayers(net.neoforged.neoforge.client.event.RegisterGuiLayersEvent event) {
        event.registerAbove(net.neoforged.neoforge.client.gui.VanillaGuiLayers.CROSSHAIR, ReikoGodzilla.id("pilot_reticle"), (graphics, delta) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.options.hideGui || mc.options.getCameraType().isFirstPerson()) return;
            if (!(mc.player != null && mc.player.getVehicle() instanceof VehicleWeapon)) return;
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            com.mojang.blaze3d.systems.RenderSystem.blendFuncSeparate(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                    com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE,
                    com.mojang.blaze3d.platform.GlStateManager.DestFactor.ZERO);
            graphics.blitSprite(net.minecraft.resources.ResourceLocation.withDefaultNamespace("hud/crosshair"),
                    (graphics.guiWidth() - 15) / 2, (graphics.guiHeight() - 15) / 2, 15, 15);
            com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
            com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        });
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

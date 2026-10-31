package com.reiko45.godzilla.registry;

import com.reiko45.godzilla.ReikoGodzilla;
import com.reiko45.godzilla.entity.kaiju.*;
import com.reiko45.godzilla.entity.mecha.*;
import com.reiko45.godzilla.entity.projectile.AcidSapEntity;
import com.reiko45.godzilla.entity.vehicle.FreezerTankEntity;
import com.reiko45.godzilla.entity.projectile.GhidorahMeteorEntity;
import com.reiko45.godzilla.entity.projectile.KaijuMissileEntity;
import com.reiko45.godzilla.entity.projectile.OxygenDestroyerEntity;
import com.reiko45.godzilla.entity.projectile.SilkShotEntity;
import com.reiko45.godzilla.entity.vehicle.MaserTankEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Sizes are at the default scale (1/5 of film size) and only cover the torso; heads, tails and wings get their own
 * hitboxes. The config scale and variant scale are applied on top through the SCALE attribute.
 */
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, ReikoGodzilla.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<GodzillaEntity>> GODZILLA = ENTITY_TYPES.register("godzilla",
            () -> EntityType.Builder.of(GodzillaEntity::new, MobCategory.CREATURE).sized(4.0f, 9.5f).eyeHeight(8.8f)
                    .fireImmune().clientTrackingRange(16).build("godzilla"));

    public static final DeferredHolder<EntityType<?>, EntityType<KingGhidorahEntity>> KING_GHIDORAH = ENTITY_TYPES.register("king_ghidorah",
            () -> EntityType.Builder.of(KingGhidorahEntity::new, MobCategory.MONSTER).sized(5.0f, 11.0f).eyeHeight(12.0f)
                    .fireImmune().clientTrackingRange(16).build("king_ghidorah"));

    public static final DeferredHolder<EntityType<?>, EntityType<MothraLarvaEntity>> MOTHRA_LARVA = ENTITY_TYPES.register("mothra_larva",
            () -> EntityType.Builder.of(MothraLarvaEntity::new, MobCategory.CREATURE).sized(3.0f, 2.4f).eyeHeight(1.8f)
                    .clientTrackingRange(12).build("mothra_larva"));

    public static final DeferredHolder<EntityType<?>, EntityType<MothraEntity>> MOTHRA = ENTITY_TYPES.register("mothra",
            () -> EntityType.Builder.of(MothraEntity::new, MobCategory.CREATURE).sized(3.0f, 3.0f).eyeHeight(2.4f)
                    .clientTrackingRange(16).build("mothra"));

    public static final DeferredHolder<EntityType<?>, EntityType<RodanEntity>> RODAN = ENTITY_TYPES.register("rodan",
            () -> EntityType.Builder.of(RodanEntity::new, MobCategory.CREATURE).sized(3.0f, 5.0f).eyeHeight(5.0f)
                    .fireImmune().clientTrackingRange(16).build("rodan"));

    public static final DeferredHolder<EntityType<?>, EntityType<AnguirusEntity>> ANGUIRUS = ENTITY_TYPES.register("anguirus",
            () -> EntityType.Builder.of(AnguirusEntity::new, MobCategory.CREATURE).sized(5.0f, 4.6f).eyeHeight(3.4f)
                    .passengerAttachments(new Vec3(0, 4.6, 0.6)).clientTrackingRange(14).build("anguirus"));

    public static final DeferredHolder<EntityType<?>, EntityType<MechagodzillaEntity>> MECHAGODZILLA = ENTITY_TYPES.register("mechagodzilla",
            () -> EntityType.Builder.of(MechagodzillaEntity::new, MobCategory.MONSTER).sized(4.0f, 9.5f).eyeHeight(9.0f)
                    .fireImmune().clientTrackingRange(16).build("mechagodzilla"));

    public static final DeferredHolder<EntityType<?>, EntityType<MaserTankEntity>> MASER_TANK = ENTITY_TYPES.register("maser_tank",
            () -> EntityType.Builder.of(MaserTankEntity::new, MobCategory.MISC).sized(2.6f, 2.0f).eyeHeight(1.6f)
                    .passengerAttachments(new Vec3(0, 1.5, -0.6)).clientTrackingRange(10).build("maser_tank"));

    public static final DeferredHolder<EntityType<?>, EntityType<SilkShotEntity>> SILK_SHOT = ENTITY_TYPES.register("silk_shot",
            () -> EntityType.Builder.<SilkShotEntity>of(SilkShotEntity::new, MobCategory.MISC).sized(0.5f, 0.5f)
                    .clientTrackingRange(8).updateInterval(5).build("silk_shot"));

    public static final DeferredHolder<EntityType<?>, EntityType<KaijuMissileEntity>> KAIJU_MISSILE = ENTITY_TYPES.register("kaiju_missile",
            () -> EntityType.Builder.<KaijuMissileEntity>of(KaijuMissileEntity::new, MobCategory.MISC).sized(0.5f, 0.5f)
                    .clientTrackingRange(8).updateInterval(2).build("kaiju_missile"));

    public static final DeferredHolder<EntityType<?>, EntityType<OxygenDestroyerEntity>> OXYGEN_DESTROYER = ENTITY_TYPES.register("oxygen_destroyer",
            () -> EntityType.Builder.<OxygenDestroyerEntity>of(OxygenDestroyerEntity::new, MobCategory.MISC).sized(0.4f, 0.4f)
                    .clientTrackingRange(8).updateInterval(5).build("oxygen_destroyer"));

    public static final DeferredHolder<EntityType<?>, EntityType<GhidorahMeteorEntity>> GHIDORAH_METEOR = ENTITY_TYPES.register("ghidorah_meteor",
            () -> EntityType.Builder.<GhidorahMeteorEntity>of(GhidorahMeteorEntity::new, MobCategory.MISC).sized(2.0f, 2.0f)
                    .fireImmune().clientTrackingRange(16).updateInterval(2).build("ghidorah_meteor"));

    // ------------------------------------------------------------------ Phase 2: Heisei
    public static final DeferredHolder<EntityType<?>, EntityType<GodzillaJuniorEntity>> GODZILLA_JUNIOR = ENTITY_TYPES.register("godzilla_junior",
            () -> EntityType.Builder.of(GodzillaJuniorEntity::new, MobCategory.CREATURE).sized(4.0f, 9.5f).eyeHeight(8.8f)
                    .clientTrackingRange(12).build("godzilla_junior"));

    public static final DeferredHolder<EntityType<?>, EntityType<BiollanteEntity>> BIOLLANTE = ENTITY_TYPES.register("biollante",
            () -> EntityType.Builder.of(BiollanteEntity::new, MobCategory.MONSTER).sized(7.0f, 15.0f).eyeHeight(13.0f)
                    .clientTrackingRange(16).build("biollante"));

    public static final DeferredHolder<EntityType<?>, EntityType<BattraLarvaEntity>> BATTRA_LARVA = ENTITY_TYPES.register("battra_larva",
            () -> EntityType.Builder.of(BattraLarvaEntity::new, MobCategory.MONSTER).sized(4.0f, 3.0f).eyeHeight(2.4f)
                    .clientTrackingRange(14).build("battra_larva"));

    public static final DeferredHolder<EntityType<?>, EntityType<BattraEntity>> BATTRA = ENTITY_TYPES.register("battra",
            () -> EntityType.Builder.of(BattraEntity::new, MobCategory.MONSTER).sized(3.5f, 3.5f).eyeHeight(2.8f)
                    .clientTrackingRange(16).build("battra"));

    public static final DeferredHolder<EntityType<?>, EntityType<SpaceGodzillaEntity>> SPACE_GODZILLA = ENTITY_TYPES.register("space_godzilla",
            () -> EntityType.Builder.of(SpaceGodzillaEntity::new, MobCategory.MONSTER).sized(4.0f, 9.5f).eyeHeight(8.8f)
                    .fireImmune().clientTrackingRange(16).build("space_godzilla"));

    public static final DeferredHolder<EntityType<?>, EntityType<SpaceCrystalEntity>> SPACE_CRYSTAL = ENTITY_TYPES.register("space_crystal",
            () -> EntityType.Builder.of(SpaceCrystalEntity::new, MobCategory.MISC).sized(2.5f, 8.0f).eyeHeight(6.0f)
                    .fireImmune().clientTrackingRange(12).build("space_crystal"));

    public static final DeferredHolder<EntityType<?>, EntityType<DestoroyahEntity>> DESTOROYAH = ENTITY_TYPES.register("destoroyah",
            () -> EntityType.Builder.of(DestoroyahEntity::new, MobCategory.MONSTER).sized(5.0f, 22.0f).eyeHeight(20.0f)
                    .fireImmune().clientTrackingRange(16).build("destoroyah"));

    public static final DeferredHolder<EntityType<?>, EntityType<DestoroyahJuvenileEntity>> DESTOROYAH_JUVENILE = ENTITY_TYPES.register("destoroyah_juvenile",
            () -> EntityType.Builder.of(DestoroyahJuvenileEntity::new, MobCategory.MONSTER).sized(1.4f, 2.2f).eyeHeight(1.8f)
                    .clientTrackingRange(8).build("destoroyah_juvenile"));

    public static final DeferredHolder<EntityType<?>, EntityType<AcidSapEntity>> ACID_SAP = ENTITY_TYPES.register("acid_sap",
            () -> EntityType.Builder.<AcidSapEntity>of(AcidSapEntity::new, MobCategory.MISC).sized(0.6f, 0.6f)
                    .clientTrackingRange(8).updateInterval(5).build("acid_sap"));

    // G-Force mecha and vehicles
    public static final DeferredHolder<EntityType<?>, EntityType<GForceMechagodzillaEntity>> GFORCE_MECHAGODZILLA = ENTITY_TYPES.register("gforce_mechagodzilla",
            () -> EntityType.Builder.of(GForceMechagodzillaEntity::new, MobCategory.MISC).sized(4.0f, 9.5f).eyeHeight(8.8f)
                    .passengerAttachments(new Vec3(0, 10.2, 0.6)).fireImmune().clientTrackingRange(16).build("gforce_mechagodzilla"));

    public static final DeferredHolder<EntityType<?>, EntityType<GarudaEntity>> GARUDA = ENTITY_TYPES.register("garuda",
            () -> EntityType.Builder.of(GarudaEntity::new, MobCategory.MISC).sized(4.0f, 1.8f).eyeHeight(1.2f)
                    .passengerAttachments(new Vec3(0, 0.9, 1.4)).fireImmune().clientTrackingRange(16).build("garuda"));

    public static final DeferredHolder<EntityType<?>, EntityType<MogueraEntity>> MOGUERA = ENTITY_TYPES.register("moguera",
            () -> EntityType.Builder.of(MogueraEntity::new, MobCategory.MISC).sized(4.0f, 9.5f).eyeHeight(8.8f)
                    .passengerAttachments(new Vec3(0, 10.2, 0.4)).fireImmune().clientTrackingRange(16).build("moguera"));

    public static final DeferredHolder<EntityType<?>, EntityType<StarFalconEntity>> STAR_FALCON = ENTITY_TYPES.register("star_falcon",
            () -> EntityType.Builder.of(StarFalconEntity::new, MobCategory.MISC).sized(5.0f, 2.2f).eyeHeight(1.4f)
                    .passengerAttachments(new Vec3(0, 1.2, 1.6)).fireImmune().clientTrackingRange(16).build("star_falcon"));

    public static final DeferredHolder<EntityType<?>, EntityType<LandMogueraEntity>> LAND_MOGUERA = ENTITY_TYPES.register("land_moguera",
            () -> EntityType.Builder.of(LandMogueraEntity::new, MobCategory.MISC).sized(5.0f, 3.2f).eyeHeight(2.6f)
                    .passengerAttachments(new Vec3(0, 2.6, 0)).fireImmune().clientTrackingRange(14).build("land_moguera"));

    public static final DeferredHolder<EntityType<?>, EntityType<SuperXEntity>> SUPER_X = ENTITY_TYPES.register("super_x",
            () -> EntityType.Builder.<SuperXEntity>of((t, l) -> new SuperXEntity(t, l, 1), MobCategory.MISC).sized(6.0f, 2.6f).eyeHeight(1.8f)
                    .passengerAttachments(new Vec3(0, 1.4, 1.0)).fireImmune().clientTrackingRange(16).build("super_x"));

    public static final DeferredHolder<EntityType<?>, EntityType<SuperXEntity>> SUPER_X2 = ENTITY_TYPES.register("super_x2",
            () -> EntityType.Builder.<SuperXEntity>of((t, l) -> new SuperXEntity(t, l, 2), MobCategory.MISC).sized(6.5f, 2.2f).eyeHeight(1.6f)
                    .passengerAttachments(new Vec3(0, 1.2, 1.2)).fireImmune().clientTrackingRange(16).build("super_x2"));

    public static final DeferredHolder<EntityType<?>, EntityType<SuperXEntity>> SUPER_X3 = ENTITY_TYPES.register("super_x3",
            () -> EntityType.Builder.<SuperXEntity>of((t, l) -> new SuperXEntity(t, l, 3), MobCategory.MISC).sized(8.0f, 2.4f).eyeHeight(1.6f)
                    .passengerAttachments(new Vec3(0, 1.3, 1.4)).fireImmune().clientTrackingRange(16).build("super_x3"));

    public static final DeferredHolder<EntityType<?>, EntityType<FreezerTankEntity>> FREEZER_TANK = ENTITY_TYPES.register("freezer_tank",
            () -> EntityType.Builder.of(FreezerTankEntity::new, MobCategory.MISC).sized(2.6f, 2.0f).eyeHeight(1.6f)
                    .passengerAttachments(new Vec3(0, 1.5, -0.6)).clientTrackingRange(10).build("freezer_tank"));

    private ModEntities() {}

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(GODZILLA.get(), GodzillaEntity.createAttributes().build());
        event.put(KING_GHIDORAH.get(), KingGhidorahEntity.createAttributes().build());
        event.put(MOTHRA_LARVA.get(), MothraLarvaEntity.createAttributes().build());
        event.put(MOTHRA.get(), MothraEntity.createAttributes().build());
        event.put(RODAN.get(), RodanEntity.createAttributes().build());
        event.put(ANGUIRUS.get(), AnguirusEntity.createAttributes().build());
        event.put(MECHAGODZILLA.get(), MechagodzillaEntity.createAttributes().build());
        event.put(MASER_TANK.get(), MaserTankEntity.createAttributes().build());
        // Phase 2
        event.put(GODZILLA_JUNIOR.get(), GodzillaJuniorEntity.createAttributes().build());
        event.put(BIOLLANTE.get(), BiollanteEntity.createAttributes().build());
        event.put(BATTRA_LARVA.get(), BattraLarvaEntity.createAttributes().build());
        event.put(BATTRA.get(), BattraEntity.createAttributes().build());
        event.put(SPACE_GODZILLA.get(), SpaceGodzillaEntity.createAttributes().build());
        event.put(SPACE_CRYSTAL.get(), SpaceCrystalEntity.createAttributes().build());
        event.put(DESTOROYAH.get(), DestoroyahEntity.createAttributes().build());
        event.put(DESTOROYAH_JUVENILE.get(), DestoroyahJuvenileEntity.createAttributes().build());
        event.put(GFORCE_MECHAGODZILLA.get(), GForceMechagodzillaEntity.createAttributes().build());
        event.put(GARUDA.get(), GarudaEntity.createAttributes().build());
        event.put(MOGUERA.get(), MogueraEntity.createAttributes().build());
        event.put(STAR_FALCON.get(), StarFalconEntity.createAttributes().build());
        event.put(LAND_MOGUERA.get(), LandMogueraEntity.createAttributes().build());
        event.put(SUPER_X.get(), SuperXEntity.createAttributes().build());
        event.put(SUPER_X2.get(), SuperXEntity.createAttributes().build());
        event.put(SUPER_X3.get(), SuperXEntity.createAttributes().build());
        event.put(FREEZER_TANK.get(), FreezerTankEntity.createAttributes().build());
    }
}

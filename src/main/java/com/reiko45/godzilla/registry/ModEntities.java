package com.reiko45.godzilla.registry;

import com.reiko45.godzilla.ReikoGodzilla;
import com.reiko45.godzilla.entity.kaiju.*;
import com.reiko45.godzilla.entity.mecha.*;
import com.reiko45.godzilla.entity.monsterverse.*;
import com.reiko45.godzilla.entity.projectile.HeavyProjectileEntity;
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
    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.kaiju.BurningGodzillaEntity>> BURNING_GODZILLA = ENTITY_TYPES.register("burning_godzilla",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.kaiju.BurningGodzillaEntity::new, MobCategory.CREATURE).sized(4.0f, 9.5f).eyeHeight(8.8f)
                    .fireImmune().clientTrackingRange(16).build("burning_godzilla"));

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

    // Phase 3: MonsterVerse
    public static final DeferredHolder<EntityType<?>, EntityType<MonsterverseGodzillaEntity>> MONSTERVERSE_GODZILLA = ENTITY_TYPES.register("monsterverse_godzilla",
            () -> EntityType.Builder.of(MonsterverseGodzillaEntity::new, MobCategory.CREATURE).sized(4.0f, 9.5f).eyeHeight(8.8f).fireImmune()
                    .clientTrackingRange(16).build("monsterverse_godzilla"));

    public static final DeferredHolder<EntityType<?>, EntityType<KongEntity>> KONG = ENTITY_TYPES.register("kong",
            () -> EntityType.Builder.of(KongEntity::new, MobCategory.CREATURE).sized(3.6f, 9.0f).eyeHeight(8.4f)
                    .clientTrackingRange(16).build("kong"));

    public static final DeferredHolder<EntityType<?>, EntityType<SukoEntity>> SUKO = ENTITY_TYPES.register("suko",
            () -> EntityType.Builder.of(SukoEntity::new, MobCategory.CREATURE).sized(3.0f, 8.0f).eyeHeight(7.4f)
                    .clientTrackingRange(12).build("suko"));

    public static final DeferredHolder<EntityType<?>, EntityType<GreatApeEntity>> GREAT_APE = ENTITY_TYPES.register("great_ape",
            () -> EntityType.Builder.of(GreatApeEntity::new, MobCategory.CREATURE).sized(1.6f, 3.8f).eyeHeight(3.2f)
                    .clientTrackingRange(10).build("great_ape"));

    public static final DeferredHolder<EntityType<?>, EntityType<SkarKingEntity>> SKAR_KING = ENTITY_TYPES.register("skar_king",
            () -> EntityType.Builder.of(SkarKingEntity::new, MobCategory.MONSTER).sized(3.6f, 9.0f).eyeHeight(8.4f)
                    .clientTrackingRange(16).build("skar_king"));

    public static final DeferredHolder<EntityType<?>, EntityType<ShimoEntity>> SHIMO = ENTITY_TYPES.register("shimo",
            () -> EntityType.Builder.of(ShimoEntity::new, MobCategory.CREATURE).sized(5.0f, 9.5f).eyeHeight(8.6f)
                    .clientTrackingRange(16).build("shimo"));

    public static final DeferredHolder<EntityType<?>, EntityType<MutoEntity>> MUTO = ENTITY_TYPES.register("muto",
            () -> EntityType.Builder.of(MutoEntity::new, MobCategory.MONSTER).sized(3.0f, 5.6f).eyeHeight(5.2f).fireImmune()
                    .clientTrackingRange(16).build("muto"));

    public static final DeferredHolder<EntityType<?>, EntityType<SkullcrawlerEntity>> SKULLCRAWLER = ENTITY_TYPES.register("skullcrawler",
            () -> EntityType.Builder.of(SkullcrawlerEntity::new, MobCategory.MONSTER).sized(1.4f, 1.8f).eyeHeight(1.5f)
                    .clientTrackingRange(10).build("skullcrawler"));

    public static final DeferredHolder<EntityType<?>, EntityType<SkullDevilEntity>> SKULL_DEVIL = ENTITY_TYPES.register("skull_devil",
            () -> EntityType.Builder.of(SkullDevilEntity::new, MobCategory.MONSTER).sized(3.0f, 5.0f).eyeHeight(4.4f)
                    .clientTrackingRange(14).build("skull_devil"));

    public static final DeferredHolder<EntityType<?>, EntityType<WarbatEntity>> WARBAT = ENTITY_TYPES.register("warbat",
            () -> EntityType.Builder.of(WarbatEntity::new, MobCategory.MONSTER).sized(3.0f, 2.4f).eyeHeight(1.8f)
                    .clientTrackingRange(14).build("warbat"));

    public static final DeferredHolder<EntityType<?>, EntityType<MonsterverseGhidorahEntity>> MONSTERVERSE_KING_GHIDORAH = ENTITY_TYPES.register("monsterverse_king_ghidorah",
            () -> EntityType.Builder.of(MonsterverseGhidorahEntity::new, MobCategory.MONSTER).sized(5.0f, 11.0f).eyeHeight(12.0f).fireImmune()
                    .clientTrackingRange(16).build("monsterverse_king_ghidorah"));

    public static final DeferredHolder<EntityType<?>, EntityType<MonsterverseRodanEntity>> MONSTERVERSE_RODAN = ENTITY_TYPES.register("monsterverse_rodan",
            () -> EntityType.Builder.of(MonsterverseRodanEntity::new, MobCategory.CREATURE).sized(3.0f, 5.0f).eyeHeight(5.0f).fireImmune()
                    .clientTrackingRange(16).build("monsterverse_rodan"));

    public static final DeferredHolder<EntityType<?>, EntityType<MonsterverseMothraEntity>> MONSTERVERSE_MOTHRA = ENTITY_TYPES.register("monsterverse_mothra",
            () -> EntityType.Builder.of(MonsterverseMothraEntity::new, MobCategory.CREATURE).sized(3.0f, 3.0f).eyeHeight(2.4f)
                    .clientTrackingRange(16).build("monsterverse_mothra"));

    public static final DeferredHolder<EntityType<?>, EntityType<ScyllaEntity>> SCYLLA = ENTITY_TYPES.register("scylla",
            () -> EntityType.Builder.of(ScyllaEntity::new, MobCategory.MONSTER).sized(5.0f, 6.0f).eyeHeight(5.6f)
                    .clientTrackingRange(16).build("scylla"));

    public static final DeferredHolder<EntityType<?>, EntityType<BehemothEntity>> BEHEMOTH = ENTITY_TYPES.register("behemoth",
            () -> EntityType.Builder.of(BehemothEntity::new, MobCategory.CREATURE).sized(5.0f, 8.0f).eyeHeight(7.0f)
                    .clientTrackingRange(16).build("behemoth"));

    public static final DeferredHolder<EntityType<?>, EntityType<MethuselahEntity>> METHUSELAH = ENTITY_TYPES.register("methuselah",
            () -> EntityType.Builder.of(MethuselahEntity::new, MobCategory.CREATURE).sized(7.0f, 8.0f).eyeHeight(3.6f)
                    .clientTrackingRange(16).build("methuselah"));

    public static final DeferredHolder<EntityType<?>, EntityType<TiamatEntity>> TIAMAT = ENTITY_TYPES.register("tiamat",
            () -> EntityType.Builder.of(TiamatEntity::new, MobCategory.MONSTER).sized(3.4f, 3.0f).eyeHeight(2.2f)
                    .clientTrackingRange(16).build("tiamat"));

    public static final DeferredHolder<EntityType<?>, EntityType<ApexMechagodzillaEntity>> APEX_MECHAGODZILLA = ENTITY_TYPES.register("apex_mechagodzilla",
            () -> EntityType.Builder.of(ApexMechagodzillaEntity::new, MobCategory.MISC).sized(4.0f, 9.5f).eyeHeight(9.0f).fireImmune()
                    .passengerAttachments(new Vec3(0, 10.2, 0.6))
                    .clientTrackingRange(16).build("apex_mechagodzilla"));

    public static final DeferredHolder<EntityType<?>, EntityType<HeavEntity>> HEAV = ENTITY_TYPES.register("heav",
            () -> EntityType.Builder.of(HeavEntity::new, MobCategory.MISC).sized(3.4f, 1.8f).eyeHeight(1.2f).passengerAttachments(new Vec3(0, 1.0, 0.8))
                    .clientTrackingRange(12).build("heav"));

    public static final DeferredHolder<EntityType<?>, EntityType<HeavyProjectileEntity>> HEAVY_PROJECTILE = ENTITY_TYPES.register("heavy_projectile",
            () -> EntityType.Builder.<HeavyProjectileEntity>of(HeavyProjectileEntity::new, MobCategory.MISC).sized(0.9f, 0.9f)
                    .clientTrackingRange(8).updateInterval(10).build("heavy_projectile"));

    // Phase 4: Showa and Millennium
    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.showa.GiganEntity>> GIGAN = ENTITY_TYPES.register("gigan",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.showa.GiganEntity::new, MobCategory.MONSTER).sized(4.0f, 9.5f).eyeHeight(8.8f)
                    .clientTrackingRange(16).build("gigan"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.showa.HedorahEntity>> HEDORAH = ENTITY_TYPES.register("hedorah",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.showa.HedorahEntity::new, MobCategory.MONSTER).sized(4.0f, 12.0f).eyeHeight(10.6f).fireImmune()
                    .clientTrackingRange(16).build("hedorah"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.showa.MegalonEntity>> MEGALON = ENTITY_TYPES.register("megalon",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.showa.MegalonEntity::new, MobCategory.MONSTER).sized(4.0f, 9.5f).eyeHeight(8.8f).fireImmune()
                    .clientTrackingRange(16).build("megalon"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.showa.JetJaguarEntity>> JET_JAGUAR = ENTITY_TYPES.register("jet_jaguar",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.showa.JetJaguarEntity::new, MobCategory.CREATURE).sized(2.4f, 9.5f).eyeHeight(8.8f)
                    .clientTrackingRange(12).build("jet_jaguar"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.showa.EbirahEntity>> EBIRAH = ENTITY_TYPES.register("ebirah",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.showa.EbirahEntity::new, MobCategory.MONSTER).sized(5.0f, 3.6f).eyeHeight(2.6f)
                    .clientTrackingRange(16).build("ebirah"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.showa.KumongaEntity>> KUMONGA = ENTITY_TYPES.register("kumonga",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.showa.KumongaEntity::new, MobCategory.MONSTER).sized(6.0f, 4.0f).eyeHeight(3.0f)
                    .clientTrackingRange(16).build("kumonga"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.showa.KamacurasEntity>> KAMACURAS = ENTITY_TYPES.register("kamacuras",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.showa.KamacurasEntity::new, MobCategory.MONSTER).sized(3.0f, 5.0f).eyeHeight(4.4f)
                    .clientTrackingRange(14).build("kamacuras"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.showa.MinillaEntity>> MINILLA = ENTITY_TYPES.register("minilla",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.showa.MinillaEntity::new, MobCategory.CREATURE).sized(4.0f, 9.5f).eyeHeight(8.6f)
                    .clientTrackingRange(12).build("minilla"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.showa.GorosaurusEntity>> GOROSAURUS = ENTITY_TYPES.register("gorosaurus",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.showa.GorosaurusEntity::new, MobCategory.CREATURE).sized(4.0f, 9.5f).eyeHeight(8.8f)
                    .clientTrackingRange(16).build("gorosaurus"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.showa.MandaEntity>> MANDA = ENTITY_TYPES.register("manda",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.showa.MandaEntity::new, MobCategory.CREATURE).sized(3.0f, 3.0f).eyeHeight(2.0f)
                    .clientTrackingRange(16).build("manda"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.showa.BaragonEntity>> BARAGON = ENTITY_TYPES.register("baragon",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.showa.BaragonEntity::new, MobCategory.CREATURE).sized(4.6f, 4.4f).eyeHeight(3.4f).fireImmune()
                    .clientTrackingRange(14).build("baragon"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.showa.VaranEntity>> VARAN = ENTITY_TYPES.register("varan",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.showa.VaranEntity::new, MobCategory.CREATURE).sized(4.0f, 4.0f).eyeHeight(3.0f)
                    .clientTrackingRange(16).build("varan"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.showa.KingCaesarEntity>> KING_CAESAR = ENTITY_TYPES.register("king_caesar",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.showa.KingCaesarEntity::new, MobCategory.CREATURE).sized(4.0f, 9.5f).eyeHeight(8.8f)
                    .clientTrackingRange(16).build("king_caesar"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.showa.TitanosaurusEntity>> TITANOSAURUS = ENTITY_TYPES.register("titanosaurus",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.showa.TitanosaurusEntity::new, MobCategory.CREATURE).sized(4.0f, 9.5f).eyeHeight(8.8f)
                    .clientTrackingRange(16).build("titanosaurus"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.millennium.MillenniumGodzillaEntity>> MILLENNIUM_GODZILLA = ENTITY_TYPES.register("millennium_godzilla",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.millennium.MillenniumGodzillaEntity::new, MobCategory.CREATURE).sized(4.0f, 9.5f).eyeHeight(8.8f).fireImmune()
                    .clientTrackingRange(16).build("millennium_godzilla"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.millennium.OrgaEntity>> ORGA = ENTITY_TYPES.register("orga",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.millennium.OrgaEntity::new, MobCategory.MONSTER).sized(4.6f, 9.5f).eyeHeight(8.2f).fireImmune()
                    .clientTrackingRange(16).build("orga"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.millennium.MillennianUfoEntity>> MILLENNIAN_UFO = ENTITY_TYPES.register("millennian_ufo",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.millennium.MillennianUfoEntity::new, MobCategory.MONSTER).sized(8.0f, 3.0f).eyeHeight(1.5f).fireImmune()
                    .clientTrackingRange(16).build("millennian_ufo"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.millennium.MegaguirusEntity>> MEGAGUIRUS = ENTITY_TYPES.register("megaguirus",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.millennium.MegaguirusEntity::new, MobCategory.MONSTER).sized(3.0f, 3.0f).eyeHeight(2.4f)
                    .clientTrackingRange(16).build("megaguirus"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.millennium.MeganulonEntity>> MEGANULON = ENTITY_TYPES.register("meganulon",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.millennium.MeganulonEntity::new, MobCategory.MONSTER).sized(1.4f, 1.0f).eyeHeight(0.7f)
                    .clientTrackingRange(10).build("meganulon"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.millennium.MeganulaEntity>> MEGANULA = ENTITY_TYPES.register("meganula",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.millennium.MeganulaEntity::new, MobCategory.MONSTER).sized(1.2f, 0.6f).eyeHeight(0.3f)
                    .clientTrackingRange(10).build("meganula"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.millennium.GmkMothraEntity>> GMK_MOTHRA = ENTITY_TYPES.register("gmk_mothra",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.millennium.GmkMothraEntity::new, MobCategory.CREATURE).sized(3.0f, 3.0f).eyeHeight(2.4f)
                    .clientTrackingRange(16).build("gmk_mothra"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.millennium.GmkGhidorahEntity>> GMK_KING_GHIDORAH = ENTITY_TYPES.register("gmk_king_ghidorah",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.millennium.GmkGhidorahEntity::new, MobCategory.CREATURE).sized(5.0f, 11.0f).eyeHeight(12.0f).fireImmune()
                    .clientTrackingRange(16).build("gmk_king_ghidorah"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.millennium.KiryuEntity>> KIRYU = ENTITY_TYPES.register("kiryu",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.millennium.KiryuEntity::new, MobCategory.MISC).sized(4.0f, 9.5f).eyeHeight(9.0f).fireImmune().passengerAttachments(new Vec3(0, 10.2, 0.6))
                    .clientTrackingRange(16).build("kiryu"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.millennium.MonsterXEntity>> MONSTER_X = ENTITY_TYPES.register("monster_x",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.millennium.MonsterXEntity::new, MobCategory.MONSTER).sized(4.0f, 9.5f).eyeHeight(8.8f).fireImmune()
                    .clientTrackingRange(16).build("monster_x"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.millennium.ZillaEntity>> ZILLA = ENTITY_TYPES.register("zilla",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.millennium.ZillaEntity::new, MobCategory.MONSTER).sized(3.6f, 9.5f).eyeHeight(8.4f)
                    .clientTrackingRange(16).build("zilla"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.millennium.GotengoEntity>> GOTENGO = ENTITY_TYPES.register("gotengo",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.millennium.GotengoEntity::new, MobCategory.MISC).sized(8.0f, 6.0f).eyeHeight(4.0f).fireImmune().passengerAttachments(new Vec3(0, 6.2, 4.0))
                    .clientTrackingRange(16).build("gotengo"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.millennium.XilienSaucerEntity>> XILIEN_SAUCER = ENTITY_TYPES.register("xilien_saucer",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.millennium.XilienSaucerEntity::new, MobCategory.MONSTER).sized(3.6f, 1.2f).eyeHeight(0.6f).fireImmune()
                    .clientTrackingRange(14).build("xilien_saucer"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.projectile.WebShotEntity>> WEB_SHOT = ENTITY_TYPES.register("web_shot",
            () -> EntityType.Builder.<com.reiko45.godzilla.entity.projectile.WebShotEntity>of(com.reiko45.godzilla.entity.projectile.WebShotEntity::new, MobCategory.MISC).sized(0.5f, 0.5f)
                    .clientTrackingRange(8).updateInterval(5).build("web_shot"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.projectile.SlicerDiscEntity>> SLICER_DISC = ENTITY_TYPES.register("slicer_disc",
            () -> EntityType.Builder.<com.reiko45.godzilla.entity.projectile.SlicerDiscEntity>of(com.reiko45.godzilla.entity.projectile.SlicerDiscEntity::new, MobCategory.MISC).sized(0.9f, 0.4f)
                    .clientTrackingRange(10).updateInterval(5).build("slicer_disc"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.projectile.LightBombEntity>> LIGHT_BOMB = ENTITY_TYPES.register("light_bomb",
            () -> EntityType.Builder.<com.reiko45.godzilla.entity.projectile.LightBombEntity>of(com.reiko45.godzilla.entity.projectile.LightBombEntity::new, MobCategory.MISC).sized(0.4f, 0.4f)
                    .clientTrackingRange(10).updateInterval(5).build("light_bomb"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.projectile.MicroBlackHoleEntity>> MICRO_BLACK_HOLE = ENTITY_TYPES.register("micro_black_hole",
            () -> EntityType.Builder.<com.reiko45.godzilla.entity.projectile.MicroBlackHoleEntity>of(com.reiko45.godzilla.entity.projectile.MicroBlackHoleEntity::new, MobCategory.MISC)
                    .sized(1.0f, 1.0f).fireImmune().clientTrackingRange(16).updateInterval(2).build("micro_black_hole"));

    // Phase 5: Reiwa and modern
    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.reiwa.ShinGodzillaEntity>> SHIN_GODZILLA = ENTITY_TYPES.register("shin_godzilla",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.reiwa.ShinGodzillaEntity::new, MobCategory.MONSTER).sized(3.6f, 11.0f).eyeHeight(10.0f).fireImmune()
                    .clientTrackingRange(16).build("shin_godzilla"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.reiwa.MinusOneGodzillaEntity>> MINUS_ONE_GODZILLA = ENTITY_TYPES.register("minus_one_godzilla",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.reiwa.MinusOneGodzillaEntity::new, MobCategory.MONSTER).sized(3.6f, 10.0f).eyeHeight(9.0f).fireImmune()
                    .clientTrackingRange(16).build("minus_one_godzilla"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.reiwa.MinusZeroGhidorahEntity>> MINUS_ZERO_GHIDORAH = ENTITY_TYPES.register("minus_zero_ghidorah",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.reiwa.MinusZeroGhidorahEntity::new, MobCategory.MONSTER).sized(4.0f, 9.0f).eyeHeight(8.6f).fireImmune()
                    .clientTrackingRange(16).build("minus_zero_ghidorah"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.reiwa.GodzillaEarthEntity>> GODZILLA_EARTH = ENTITY_TYPES.register("godzilla_earth",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.reiwa.GodzillaEarthEntity::new, MobCategory.MONSTER).sized(3.6f, 10.0f).eyeHeight(9.0f).fireImmune()
                    .clientTrackingRange(16).build("godzilla_earth"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.reiwa.ServumEntity>> SERVUM = ENTITY_TYPES.register("servum",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.reiwa.ServumEntity::new, MobCategory.MONSTER).sized(3.0f, 2.6f).eyeHeight(2.0f)
                    .clientTrackingRange(16).build("servum"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.reiwa.VultureEntity>> VULTURE = ENTITY_TYPES.register("vulture",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.reiwa.VultureEntity::new, MobCategory.MISC).sized(1.4f, 2.6f).eyeHeight(2.2f).fireImmune()
                    .clientTrackingRange(16).build("vulture"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.reiwa.MechagodzillaCityEntity>> MECHAGODZILLA_CITY = ENTITY_TYPES.register("mechagodzilla_city",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.reiwa.MechagodzillaCityEntity::new, MobCategory.MONSTER).sized(6.0f, 8.0f).eyeHeight(6.0f).fireImmune()
                    .clientTrackingRange(16).build("mechagodzilla_city"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.reiwa.PlanetEaterGhidorahEntity>> PLANET_EATER_GHIDORAH = ENTITY_TYPES.register("planet_eater_ghidorah",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.reiwa.PlanetEaterGhidorahEntity::new, MobCategory.MONSTER).sized(2.0f, 2.0f).eyeHeight(1.5f).fireImmune()
                    .clientTrackingRange(16).build("planet_eater_ghidorah"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.reiwa.SingularGodzillaEntity>> SINGULAR_GODZILLA = ENTITY_TYPES.register("singular_godzilla",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.reiwa.SingularGodzillaEntity::new, MobCategory.MONSTER).sized(3.6f, 11.0f).eyeHeight(10.0f).fireImmune()
                    .clientTrackingRange(16).build("singular_godzilla"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.reiwa.JetJaguarPPEntity>> JET_JAGUAR_PP = ENTITY_TYPES.register("jet_jaguar_pp",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.reiwa.JetJaguarPPEntity::new, MobCategory.MISC).sized(3.0f, 10.0f).eyeHeight(9.0f).fireImmune()
                    .clientTrackingRange(16).build("jet_jaguar_pp"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.reiwa.SingularRodanEntity>> SINGULAR_RODAN = ENTITY_TYPES.register("singular_rodan",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.reiwa.SingularRodanEntity::new, MobCategory.MONSTER).sized(3.0f, 5.0f).eyeHeight(4.6f)
                    .clientTrackingRange(16).build("singular_rodan"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.reiwa.SalungaEntity>> SALUNGA = ENTITY_TYPES.register("salunga",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.reiwa.SalungaEntity::new, MobCategory.MONSTER).sized(3.0f, 8.0f).eyeHeight(7.0f)
                    .clientTrackingRange(16).build("salunga"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.reiwa.TitanXEntity>> TITAN_X = ENTITY_TYPES.register("titan_x",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.reiwa.TitanXEntity::new, MobCategory.MONSTER).sized(5.0f, 5.0f).eyeHeight(4.2f)
                    .clientTrackingRange(16).build("titan_x"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.reiwa.ScarabEntity>> SCARAB = ENTITY_TYPES.register("scarab",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.reiwa.ScarabEntity::new, MobCategory.CREATURE).sized(0.7f, 0.4f).eyeHeight(0.3f)
                    .clientTrackingRange(10).build("scarab"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.reiwa.FrostVarkEntity>> FROST_VARK = ENTITY_TYPES.register("frost_vark",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.reiwa.FrostVarkEntity::new, MobCategory.MONSTER).sized(3.0f, 3.0f).eyeHeight(2.5f)
                    .clientTrackingRange(16).build("frost_vark"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.reiwa.IonDragonEntity>> ION_DRAGON = ENTITY_TYPES.register("ion_dragon",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.reiwa.IonDragonEntity::new, MobCategory.MONSTER).sized(4.0f, 2.0f).eyeHeight(1.5f)
                    .clientTrackingRange(16).build("ion_dragon"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.reiwa.KrakenEntity>> KRAKEN = ENTITY_TYPES.register("kraken",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.reiwa.KrakenEntity::new, MobCategory.MONSTER).sized(4.0f, 3.0f).eyeHeight(2.5f)
                    .clientTrackingRange(16).build("kraken"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.reiwa.ShindenEntity>> SHINDEN = ENTITY_TYPES.register("shinden",
            () -> EntityType.Builder.of(com.reiko45.godzilla.entity.reiwa.ShindenEntity::new, MobCategory.MISC).sized(2.0f, 1.0f).eyeHeight(0.8f)
                    .clientTrackingRange(16).build("shinden"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.projectile.TrainBombEntity>> TRAIN_BOMB = ENTITY_TYPES.register("train_bomb",
            () -> EntityType.Builder.<com.reiko45.godzilla.entity.projectile.TrainBombEntity>of(com.reiko45.godzilla.entity.projectile.TrainBombEntity::new, MobCategory.MISC)
                    .sized(1.4f, 1.0f).clientTrackingRange(10).updateInterval(2).build("train_bomb"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reiko45.godzilla.entity.projectile.DroneEntity>> DRONE = ENTITY_TYPES.register("drone",
            () -> EntityType.Builder.<com.reiko45.godzilla.entity.projectile.DroneEntity>of(com.reiko45.godzilla.entity.projectile.DroneEntity::new, MobCategory.MISC)
                    .sized(0.6f, 0.4f).clientTrackingRange(10).updateInterval(2).build("drone"));

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
        event.put(BURNING_GODZILLA.get(), com.reiko45.godzilla.entity.kaiju.BurningGodzillaEntity.createAttributes().build());
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
        // Phase 3
        event.put(MONSTERVERSE_GODZILLA.get(), MonsterverseGodzillaEntity.createAttributes().build());
        event.put(KONG.get(), KongEntity.createAttributes().build());
        event.put(SUKO.get(), SukoEntity.createAttributes().build());
        event.put(GREAT_APE.get(), GreatApeEntity.createAttributes().build());
        event.put(SKAR_KING.get(), SkarKingEntity.createAttributes().build());
        event.put(SHIMO.get(), ShimoEntity.createAttributes().build());
        event.put(MUTO.get(), MutoEntity.createAttributes().build());
        event.put(SKULLCRAWLER.get(), SkullcrawlerEntity.createAttributes().build());
        event.put(SKULL_DEVIL.get(), SkullDevilEntity.createAttributes().build());
        event.put(WARBAT.get(), WarbatEntity.createAttributes().build());
        event.put(MONSTERVERSE_KING_GHIDORAH.get(), MonsterverseGhidorahEntity.createAttributes().build());
        event.put(MONSTERVERSE_RODAN.get(), MonsterverseRodanEntity.createAttributes().build());
        event.put(MONSTERVERSE_MOTHRA.get(), MonsterverseMothraEntity.createAttributes().build());
        event.put(SCYLLA.get(), ScyllaEntity.createAttributes().build());
        event.put(BEHEMOTH.get(), BehemothEntity.createAttributes().build());
        event.put(METHUSELAH.get(), MethuselahEntity.createAttributes().build());
        event.put(TIAMAT.get(), TiamatEntity.createAttributes().build());
        event.put(APEX_MECHAGODZILLA.get(), ApexMechagodzillaEntity.createAttributes().build());
        event.put(HEAV.get(), HeavEntity.createAttributes().build());
        // Phase 4
        event.put(GIGAN.get(), com.reiko45.godzilla.entity.showa.GiganEntity.createAttributes().build());
        event.put(HEDORAH.get(), com.reiko45.godzilla.entity.showa.HedorahEntity.createAttributes().build());
        event.put(MEGALON.get(), com.reiko45.godzilla.entity.showa.MegalonEntity.createAttributes().build());
        event.put(JET_JAGUAR.get(), com.reiko45.godzilla.entity.showa.JetJaguarEntity.createAttributes().build());
        event.put(EBIRAH.get(), com.reiko45.godzilla.entity.showa.EbirahEntity.createAttributes().build());
        event.put(KUMONGA.get(), com.reiko45.godzilla.entity.showa.KumongaEntity.createAttributes().build());
        event.put(KAMACURAS.get(), com.reiko45.godzilla.entity.showa.KamacurasEntity.createAttributes().build());
        event.put(MINILLA.get(), com.reiko45.godzilla.entity.showa.MinillaEntity.createAttributes().build());
        event.put(GOROSAURUS.get(), com.reiko45.godzilla.entity.showa.GorosaurusEntity.createAttributes().build());
        event.put(MANDA.get(), com.reiko45.godzilla.entity.showa.MandaEntity.createAttributes().build());
        event.put(BARAGON.get(), com.reiko45.godzilla.entity.showa.BaragonEntity.createAttributes().build());
        event.put(VARAN.get(), com.reiko45.godzilla.entity.showa.VaranEntity.createAttributes().build());
        event.put(KING_CAESAR.get(), com.reiko45.godzilla.entity.showa.KingCaesarEntity.createAttributes().build());
        event.put(TITANOSAURUS.get(), com.reiko45.godzilla.entity.showa.TitanosaurusEntity.createAttributes().build());
        event.put(MILLENNIUM_GODZILLA.get(), com.reiko45.godzilla.entity.millennium.MillenniumGodzillaEntity.createAttributes().build());
        event.put(ORGA.get(), com.reiko45.godzilla.entity.millennium.OrgaEntity.createAttributes().build());
        event.put(MILLENNIAN_UFO.get(), com.reiko45.godzilla.entity.millennium.MillennianUfoEntity.createAttributes().build());
        event.put(MEGAGUIRUS.get(), com.reiko45.godzilla.entity.millennium.MegaguirusEntity.createAttributes().build());
        event.put(MEGANULON.get(), com.reiko45.godzilla.entity.millennium.MeganulonEntity.createAttributes().build());
        event.put(MEGANULA.get(), com.reiko45.godzilla.entity.millennium.MeganulaEntity.createAttributes().build());
        event.put(GMK_MOTHRA.get(), com.reiko45.godzilla.entity.millennium.GmkMothraEntity.createAttributes().build());
        event.put(GMK_KING_GHIDORAH.get(), com.reiko45.godzilla.entity.millennium.GmkGhidorahEntity.createAttributes().build());
        event.put(KIRYU.get(), com.reiko45.godzilla.entity.millennium.KiryuEntity.createAttributes().build());
        event.put(MONSTER_X.get(), com.reiko45.godzilla.entity.millennium.MonsterXEntity.createAttributes().build());
        event.put(ZILLA.get(), com.reiko45.godzilla.entity.millennium.ZillaEntity.createAttributes().build());
        event.put(GOTENGO.get(), com.reiko45.godzilla.entity.millennium.GotengoEntity.createAttributes().build());
        event.put(XILIEN_SAUCER.get(), com.reiko45.godzilla.entity.millennium.XilienSaucerEntity.createAttributes().build());
        event.put(SHIN_GODZILLA.get(), com.reiko45.godzilla.entity.reiwa.ShinGodzillaEntity.createAttributes().build());
        event.put(MINUS_ONE_GODZILLA.get(), com.reiko45.godzilla.entity.reiwa.MinusOneGodzillaEntity.createAttributes().build());
        event.put(MINUS_ZERO_GHIDORAH.get(), com.reiko45.godzilla.entity.reiwa.MinusZeroGhidorahEntity.createAttributes().build());
        event.put(GODZILLA_EARTH.get(), com.reiko45.godzilla.entity.reiwa.GodzillaEarthEntity.createAttributes().build());
        event.put(SERVUM.get(), com.reiko45.godzilla.entity.reiwa.ServumEntity.createAttributes().build());
        event.put(VULTURE.get(), com.reiko45.godzilla.entity.reiwa.VultureEntity.createAttributes().build());
        event.put(MECHAGODZILLA_CITY.get(), com.reiko45.godzilla.entity.reiwa.MechagodzillaCityEntity.createAttributes().build());
        event.put(PLANET_EATER_GHIDORAH.get(), com.reiko45.godzilla.entity.reiwa.PlanetEaterGhidorahEntity.createAttributes().build());
        event.put(SINGULAR_GODZILLA.get(), com.reiko45.godzilla.entity.reiwa.SingularGodzillaEntity.createAttributes().build());
        event.put(JET_JAGUAR_PP.get(), com.reiko45.godzilla.entity.reiwa.JetJaguarPPEntity.createAttributes().build());
        event.put(SINGULAR_RODAN.get(), com.reiko45.godzilla.entity.reiwa.SingularRodanEntity.createAttributes().build());
        event.put(SALUNGA.get(), com.reiko45.godzilla.entity.reiwa.SalungaEntity.createAttributes().build());
        event.put(TITAN_X.get(), com.reiko45.godzilla.entity.reiwa.TitanXEntity.createAttributes().build());
        event.put(SCARAB.get(), com.reiko45.godzilla.entity.reiwa.ScarabEntity.createAttributes().build());
        event.put(FROST_VARK.get(), com.reiko45.godzilla.entity.reiwa.FrostVarkEntity.createAttributes().build());
        event.put(ION_DRAGON.get(), com.reiko45.godzilla.entity.reiwa.IonDragonEntity.createAttributes().build());
        event.put(KRAKEN.get(), com.reiko45.godzilla.entity.reiwa.KrakenEntity.createAttributes().build());
        event.put(SHINDEN.get(), com.reiko45.godzilla.entity.reiwa.ShindenEntity.createAttributes().build());
    }

    /** Where mobs that spawn naturally (Skullcrawlers, Great Apes, Warbats in the Hollow Earth) may appear. */
    public static void registerSpawnPlacements(net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent event) {
        var op = net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE;
        event.register(SKULLCRAWLER.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules, op);
        event.register(GREAT_APE.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.Mob::checkMobSpawnRules, op);
        event.register(WARBAT.get(), net.minecraft.world.entity.SpawnPlacementTypes.NO_RESTRICTIONS,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> random.nextInt(6) == 0 && level.getBlockState(pos).isAir(), op);
    }
}

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

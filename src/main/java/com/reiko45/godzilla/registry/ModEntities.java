package com.reiko45.godzilla.registry;

import com.reiko45.godzilla.ReikoGodzilla;
import com.reiko45.godzilla.entity.kaiju.*;
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
    }
}

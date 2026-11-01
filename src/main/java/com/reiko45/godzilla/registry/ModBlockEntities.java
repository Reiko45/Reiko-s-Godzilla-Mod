package com.reiko45.godzilla.registry;

import com.reiko45.godzilla.ReikoGodzilla;
import com.reiko45.godzilla.block.KaijuLairBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ReikoGodzilla.MODID);

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<KaijuLairBlockEntity>> KAIJU_LAIR = BLOCK_ENTITIES.register("kaiju_lair",
            () -> BlockEntityType.Builder.of(KaijuLairBlockEntity::new, ModBlocks.KAIJU_LAIR.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.reiko45.godzilla.block.RadiationSourceBlockEntity>> RADIATION_SOURCE =
            BLOCK_ENTITIES.register("radiation_source", () -> BlockEntityType.Builder.of(com.reiko45.godzilla.block.RadiationSourceBlockEntity::new,
                    ModBlocks.NUCLEAR_WARHEAD.get(), ModBlocks.SOLAR_CRYSTAL.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.reiko45.godzilla.block.OrcaBlockEntity>> ORCA =
            BLOCK_ENTITIES.register("orca", () -> BlockEntityType.Builder.of(com.reiko45.godzilla.block.OrcaBlockEntity::new, ModBlocks.ORCA.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.reiko45.godzilla.block.StormWallBlockEntity>> STORM_WALL =
            BLOCK_ENTITIES.register("storm_wall", () -> BlockEntityType.Builder.of(com.reiko45.godzilla.block.StormWallBlockEntity::new, ModBlocks.STORM_WALL.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.reiko45.godzilla.block.AlienTransmitterBlockEntity>> ALIEN_TRANSMITTER =
            BLOCK_ENTITIES.register("alien_transmitter", () -> BlockEntityType.Builder.of(com.reiko45.godzilla.block.AlienTransmitterBlockEntity::new, ModBlocks.ALIEN_TRANSMITTER.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.reiko45.godzilla.block.ShisaStatueBlockEntity>> SHISA_STATUE =
            BLOCK_ENTITIES.register("shisa_statue", () -> BlockEntityType.Builder.of(com.reiko45.godzilla.block.ShisaStatueBlockEntity::new, ModBlocks.SHISA_STATUE.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.reiko45.godzilla.block.GuardianShrineBlockEntity>> GUARDIAN_SHRINE =
            BLOCK_ENTITIES.register("guardian_shrine", () -> BlockEntityType.Builder.of(com.reiko45.godzilla.block.GuardianShrineBlockEntity::new, ModBlocks.GUARDIAN_SHRINE.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.reiko45.godzilla.block.KiryuChargerBlockEntity>> KIRYU_CHARGER =
            BLOCK_ENTITIES.register("kiryu_charger", () -> BlockEntityType.Builder.of(com.reiko45.godzilla.block.KiryuChargerBlockEntity::new, ModBlocks.KIRYU_CHARGER.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.reiko45.godzilla.block.TowerLaserBlockEntity>> TOWER_LASER =
            BLOCK_ENTITIES.register("tower_laser", () -> BlockEntityType.Builder.of(com.reiko45.godzilla.block.TowerLaserBlockEntity::new, ModBlocks.TOWER_LASER.get()).build(null));

    private ModBlockEntities() {}
}

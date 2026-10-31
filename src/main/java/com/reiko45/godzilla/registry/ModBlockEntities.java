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

    private ModBlockEntities() {}
}

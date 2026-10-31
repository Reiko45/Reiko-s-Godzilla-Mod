package com.reiko45.godzilla.block;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class KaijuLairBlockEntity extends BlockEntity {
    private static final double TRIGGER_RANGE = 48;
    @Nullable
    private ResourceLocation kaiju;
    private int variant = -1;

    public KaijuLairBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.KAIJU_LAIR.get(), pos, state);
    }

    public void setKaiju(ResourceLocation entityType, int variant) {
        this.kaiju = entityType;
        this.variant = variant;
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, KaijuLairBlockEntity lair) {
        if (level.getGameTime() % 20 != 0 || !(level instanceof ServerLevel server)) return;
        Player player = level.getNearestPlayer(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, TRIGGER_RANGE, p -> !p.isSpectator());
        if (player == null) return;
        if (lair.kaiju != null) {
            Entity entity = BuiltInRegistries.ENTITY_TYPE.get(lair.kaiju).create(server);
            if (entity instanceof Mob mob) {
                mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360f, 0);
                mob.finalizeSpawn(server, server.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
                if (lair.variant >= 0 && mob instanceof KaijuEntity k) {
                    k.setVariant(lair.variant);
                    k.setHealth(k.getMaxHealth());
                }
                server.addFreshEntity(mob);
            }
        }
        level.removeBlock(pos, false);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (kaiju != null) tag.putString("Kaiju", kaiju.toString());
        tag.putInt("Variant", variant);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        kaiju = tag.contains("Kaiju") ? ResourceLocation.tryParse(tag.getString("Kaiju")) : null;
        variant = tag.contains("Variant") ? tag.getInt("Variant") : -1;
    }
}

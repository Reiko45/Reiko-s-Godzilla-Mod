package com.reiko45.godzilla.block;

import com.reiko45.godzilla.entity.FlyingKaijuEntity;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.KaijuFaction;
import com.reiko45.godzilla.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Skull Island's storm wall (Kong: Skull Island): a ring of lightning around the island that tears up aircraft trying
 * to cross it. Only runs while a player is near.
 */
public class StormWallBlockEntity extends BlockEntity {
    private int radius = 80;

    public StormWallBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STORM_WALL.get(), pos, state);
    }

    public void setRadius(int radius) {
        this.radius = radius;
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, StormWallBlockEntity wall) {
        if (!(level instanceof ServerLevel server) || level.getGameTime() % 25 != 0) return;
        if (server.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), wall.radius + 96, false) == null) return;
        var random = server.getRandom();
        double a = random.nextDouble() * Math.PI * 2;
        double r = wall.radius + (random.nextDouble() - 0.5) * 16;
        int x = pos.getX() + (int) (Math.cos(a) * r), z = pos.getZ() + (int) (Math.sin(a) * r);
        if (!server.hasChunkAt(new BlockPos(x, 0, z))) return;
        int y = server.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(server);
        if (bolt != null) {
            bolt.moveTo(Vec3.atBottomCenterOf(new BlockPos(x, y, z)));
            bolt.setVisualOnly(true);
            server.addFreshEntity(bolt);
        }
        // Aircraft inside the wall get battered.
        AABB ring = new AABB(pos).inflate(wall.radius + 12, 128, wall.radius + 12);
        for (KaijuEntity k : server.getEntitiesOfClass(KaijuEntity.class, ring, k -> k instanceof FlyingKaijuEntity f && f.isFlying() && k.getFaction() == KaijuFaction.HUMAN)) {
            double d = Math.sqrt(Math.pow(k.getX() - pos.getX(), 2) + Math.pow(k.getZ() - pos.getZ(), 2));
            if (Math.abs(d - wall.radius) < 14) {
                k.hurt(server.damageSources().lightningBolt(), 10f);
                k.push((random.nextDouble() - 0.5) * 1.5, -0.4, (random.nextDouble() - 0.5) * 1.5);
                if (k.getControllingPassenger() instanceof Player p) {
                    p.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.reikogodzilla.storm_wall")
                            .withStyle(net.minecraft.ChatFormatting.YELLOW), true);
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Radius", radius);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Radius")) radius = tag.getInt("Radius");
    }
}

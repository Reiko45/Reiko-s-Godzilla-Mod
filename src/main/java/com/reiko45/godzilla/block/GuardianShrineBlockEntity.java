package com.reiko45.godzilla.block;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.ShrineGuardian;
import com.reiko45.godzilla.entity.millennium.MillenniumGodzillaEntity;
import com.reiko45.godzilla.entity.showa.BaragonEntity;
import com.reiko45.godzilla.registry.ModBlockEntities;
import com.reiko45.godzilla.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;

public class GuardianShrineBlockEntity extends BlockEntity {
    private boolean awakened;

    public GuardianShrineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GUARDIAN_SHRINE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GuardianShrineBlockEntity shrine) {
        if (shrine.awakened || level.getGameTime() % 40 != 0 || !(level instanceof ServerLevel server)) return;
        boolean threat = !server.getEntitiesOfClass(MillenniumGodzillaEntity.class, new AABB(pos).inflate(128),
                g -> g.getVariant() == MillenniumGodzillaEntity.GMK && g.isAlive()).isEmpty();
        if (threat) shrine.awaken(null);
    }

    /** Raise the Guardian: to fight GMK Godzilla, or to punish {@code vandal}. */
    public void awaken(@Nullable Player vandal) {
        if (awakened || !(level instanceof ServerLevel server)) return;
        awakened = true;
        setChanged();
        GuardianShrineBlock.Guardian which = getBlockState().getValue(GuardianShrineBlock.GUARDIAN);
        KaijuEntity guardian = switch (which) {
            case BARAGON -> ModEntities.BARAGON.get().create(server);
            case MOTHRA -> ModEntities.GMK_MOTHRA.get().create(server);
            case GHIDORAH -> ModEntities.GMK_KING_GHIDORAH.get().create(server);
        };
        if (guardian == null) return;
        BlockPos pos = getBlockPos();
        int x = pos.getX() + server.random.nextInt(17) - 8, z = pos.getZ() + server.random.nextInt(17) - 8;
        int y = which == GuardianShrineBlock.Guardian.MOTHRA ? pos.getY() + 12 : server.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        guardian.moveTo(x + 0.5, y, z + 0.5, server.random.nextFloat() * 360f, 0);
        guardian.finalizeSpawn(server, server.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
        if (guardian instanceof BaragonEntity baragon) {
            baragon.setVariant(BaragonEntity.GMK);
            baragon.setHealth(baragon.getMaxHealth());
        }
        server.addFreshEntity(guardian);
        if (guardian instanceof ShrineGuardian g) g.awaken(vandal);
        server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x + 0.5, y + 1, z + 0.5, 2, 1, 1, 1, 0);
        server.playSound(null, pos, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 4f, 0.6f);
        String key = vandal != null ? "message.reikogodzilla.guardian_vengeance" : "message.reikogodzilla.guardian_rises";
        for (Player p : server.players()) {
            if (p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 200 * 200) {
                p.sendSystemMessage(Component.translatable(key, guardian.getDisplayName()).withStyle(vandal != null ? ChatFormatting.DARK_RED : ChatFormatting.GOLD));
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("Awakened", awakened);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        awakened = tag.getBoolean("Awakened");
    }
}

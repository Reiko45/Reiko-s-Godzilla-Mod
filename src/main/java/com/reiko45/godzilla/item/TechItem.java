package com.reiko45.godzilla.item;

import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.projectile.KaijuMissileEntity;
import com.reiko45.godzilla.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The Phase 5 human tech, one class with an action per item: Operation Yashiori's coagulant pump, train bombs, drone
 * swarms and MOP II strike designator; Operation Wada Tsumi's Freon tanks and balloon rig; the radiation buoy (a kaiju
 * tracker); Ghidorah's gravity gland; the anime trilogy's EMP probes; and the Orthogonal Diagonalizer.
 */
public class TechItem extends Item {
    public enum Kind { COAGULANT_PUMP, TRAIN_BOMB, DRONE_SWARM, MOP_DESIGNATOR, FREON_TANK, BALLOON_RIG, RADIATION_BUOY, GRAVITY_GLAND, EMP_PROBE, DIAGONALIZER }

    private final Kind kind;

    public TechItem(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server)) return InteractionResultHolder.success(stack);
        boolean used = switch (kind) {
            case COAGULANT_PUMP -> pump(server, player);
            case TRAIN_BOMB -> trainBomb(server, player);
            case DRONE_SWARM -> drones(server, player);
            case MOP_DESIGNATOR -> mop(server, player);
            case FREON_TANK -> wadaTsumi(server, player, true);
            case BALLOON_RIG -> wadaTsumi(server, player, false);
            case RADIATION_BUOY -> buoy(server, player);
            case GRAVITY_GLAND -> gland(server, player);
            case EMP_PROBE -> probe(server, player);
            case DIAGONALIZER -> diagonalize(server, player);
        };
        if (!used) return InteractionResultHolder.fail(stack);
        switch (kind) {
            case COAGULANT_PUMP, RADIATION_BUOY, GRAVITY_GLAND -> {}
            case DIAGONALIZER -> stack.hurtAndBreak(1, player, net.minecraft.world.entity.LivingEntity.getSlotForHand(hand));
            default -> stack.consume(1, player);
        }
        return InteractionResultHolder.consume(stack);
    }

    private static void say(Player player, String key, Object... args) {
        player.displayClientMessage(Component.translatable("message.reikogodzilla." + key, args), true);
    }

    /** Pumps one coagulant canister from the inventory into a downed kaiju's mouth nearby. */
    private boolean pump(ServerLevel level, Player player) {
        var shins = level.getEntitiesOfClass(com.reiko45.godzilla.entity.reiwa.ShinGodzillaEntity.class, player.getBoundingBox().inflate(48),
                s -> s.getBeamOrigin(0, 1f).distanceTo(player.position()) < 14 + s.getBbWidth());
        if (shins.isEmpty()) {
            say(player, "pump_no_target");
            return false;
        }
        int slot = player.getInventory().findSlotMatchingItem(new ItemStack(ModItems.COAGULANT_CANISTER.get()));
        if (slot < 0 && !player.getAbilities().instabuild) {
            say(player, "pump_no_canister");
            return false;
        }
        var shin = shins.get(0);
        if (!shin.pumpCoagulant(5f, player)) {
            say(player, "pump_not_down");
            return false;
        }
        if (slot >= 0 && !player.getAbilities().instabuild) player.getInventory().removeItem(slot, 1);
        Vec3 from = player.getEyePosition(), to = shin.getBeamOrigin(0, 1f);
        for (int i = 0; i <= 20; i++) {
            Vec3 p = from.lerp(to, i / 20.0);
            level.sendParticles(ParticleTypes.FALLING_DRIPSTONE_WATER, p.x, p.y, p.z, 2, 0.1, 0.1, 0.1, 0);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 1.5f, 0.6f);
        player.getCooldowns().addCooldown(this, 20);
        return true;
    }

    private boolean trainBomb(ServerLevel level, Player player) {
        Entity target = ThrowableKaijuItem.lookTarget(player, 128);
        Vec3 look = player.getLookAngle().multiply(1, 0, 1).normalize();
        for (int i = 0; i < 3; i++) {
            var train = new com.reiko45.godzilla.entity.projectile.TrainBombEntity(level, player, target);
            Vec3 at = player.position().add(look.scale(2 - i * 2.2)).add(0, 0.6, 0);
            train.setPos(at.x, at.y, at.z);
            train.setDeltaMovement(look.scale(0.9));
            level.addFreshEntity(train);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.MINECART_RIDING, SoundSource.PLAYERS, 2f, 0.6f);
        player.getCooldowns().addCooldown(this, 100);
        return true;
    }

    private boolean drones(ServerLevel level, Player player) {
        for (int i = 0; i < 6; i++) {
            var drone = new com.reiko45.godzilla.entity.projectile.DroneEntity(level, player);
            drone.setPos(player.getX() + (level.random.nextDouble() - 0.5) * 3, player.getY() + 2 + i * 0.4, player.getZ() + (level.random.nextDouble() - 0.5) * 3);
            drone.setDeltaMovement(0, 0.6, 0);
            level.addFreshEntity(drone);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.BEE_LOOP, SoundSource.PLAYERS, 2f, 2f);
        player.getCooldowns().addCooldown(this, 200);
        return true;
    }

    /** Calls in three bunker-buster bombs on the spot you're looking at. */
    private boolean mop(ServerLevel level, Player player) {
        Vec3 eye = player.getEyePosition();
        var hit = level.clip(new net.minecraft.world.level.ClipContext(eye, eye.add(player.getLookAngle().scale(256)),
                net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, player));
        Entity target = ThrowableKaijuItem.lookTarget(player, 256);
        Vec3 at = target != null ? target.position().add(0, target.getBbHeight() * 0.7, 0) : hit.getLocation();
        for (int i = 0; i < 3; i++) {
            var bomb = new KaijuMissileEntity(level, at.x + (i - 1) * 1.5, at.y + 90 + i * 8, at.z + (i - 1) * 1.5, KaijuMissileEntity.Payload.MOP);
            bomb.setOwner(player);
            bomb.shoot(0, -1, 0, 2.2f, 0.5f);
            level.addFreshEntity(bomb);
        }
        say(player, "mop_inbound");
        level.playSound(null, player.blockPosition(), SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 1f, 0.5f);
        player.getCooldowns().addCooldown(this, com.reiko45.godzilla.GodzillaConfig.get(com.reiko45.godzilla.GodzillaConfig.MOP_COOLDOWN, 12000));
        return true;
    }

    private boolean wadaTsumi(ServerLevel level, Player player, boolean freon) {
        var targets = level.getEntitiesOfClass(com.reiko45.godzilla.entity.reiwa.MinusOneGodzillaEntity.class, player.getBoundingBox().inflate(32));
        if (targets.isEmpty()) {
            say(player, "wada_tsumi_no_target");
            return false;
        }
        var godzilla = targets.get(0);
        boolean ok = freon ? godzilla.attachFreon(player) : godzilla.releaseBalloons(player);
        if (ok) player.getCooldowns().addCooldown(this, 40);
        return ok;
    }

    private boolean buoy(ServerLevel level, Player player) {
        KaijuEntity nearest = null;
        double best = 256 * 256;
        for (KaijuEntity k : level.getEntitiesOfClass(KaijuEntity.class, player.getBoundingBox().inflate(256), k -> k.isAlive() && k.getBbHeight() > 3)) {
            double d = k.distanceToSqr(player);
            if (d < best) {
                best = d;
                nearest = k;
            }
        }
        if (nearest == null) {
            say(player, "buoy_none");
        } else {
            double dx = nearest.getX() - player.getX(), dz = nearest.getZ() - player.getZ();
            String[] dirs = {"south", "south_west", "west", "north_west", "north", "north_east", "east", "south_east"};
            int i = Math.floorMod((int) Math.round(Math.toDegrees(Math.atan2(-dx, dz)) / 45.0), 8);
            player.displayClientMessage(Component.translatable("message.reikogodzilla.buoy", nearest.getDisplayName(), (int) Math.sqrt(best),
                    Component.translatable("direction.reikogodzilla." + dirs[i])).withStyle(ChatFormatting.GREEN), true);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 1f, 1.6f);
        player.getCooldowns().addCooldown(this, 40);
        return true;
    }

    private boolean gland(ServerLevel level, Player player) {
        player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 2));
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 240, 0));
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY(), player.getZ(), 30, 0.5, 0.2, 0.5, 0.1);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1f, 1.8f);
        player.getCooldowns().addCooldown(this, 200);
        return true;
    }

    private boolean probe(ServerLevel level, Player player) {
        var probe = new KaijuMissileEntity(level, player, KaijuMissileEntity.Payload.EMP_PROBE);
        Vec3 eye = player.getEyePosition().add(player.getLookAngle().scale(1.2));
        probe.setPos(eye.x, eye.y - 0.2, eye.z);
        probe.shootFromRotation(player, player.getXRot(), player.getYRot(), 0f, 2.6f, 0.3f);
        level.addFreshEntity(probe);
        level.playSound(null, player.blockPosition(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.5f, 0.6f);
        player.getCooldowns().addCooldown(this, 20);
        return true;
    }

    /** A prototype pulse: crystallizes Red Dust nearby and disrupts any Singular Point Godzilla in range. */
    private boolean diagonalize(ServerLevel level, Player player) {
        int crystal = player.getInventory().findSlotMatchingItem(new ItemStack(ModItems.ARCHETYPE_CRYSTAL.get()));
        if (crystal < 0 && !player.getAbilities().instabuild) {
            say(player, "diagonalizer_needs_crystal");
            return false;
        }
        if (crystal >= 0 && !player.getAbilities().instabuild) player.getInventory().removeItem(crystal, 1);
        int cleared = com.reiko45.godzilla.block.RedDustBlock.crystallize(level, player.blockPosition(), 24);
        for (var g : level.getEntitiesOfClass(com.reiko45.godzilla.entity.reiwa.SingularGodzillaEntity.class, player.getBoundingBox().inflate(48))) g.diagonalizerHit(player);
        say(player, "diagonalizer_pulse", cleared);
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1, player.getZ(), 80, 6, 2, 6, 0.1);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 2f, 0.8f);
        player.getCooldowns().addCooldown(this, 200);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.reikogodzilla." + net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(this).getPath())
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}

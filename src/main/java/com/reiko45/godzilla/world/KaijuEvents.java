package com.reiko45.godzilla.world;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.ReikoGodzilla;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.kaiju.GodzillaEntity;
import com.reiko45.godzilla.entity.kaiju.MechagodzillaEntity;
import com.reiko45.godzilla.entity.projectile.GhidorahMeteorEntity;
import com.reiko45.godzilla.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.List;

/**
 * Random kaiju events and the /kaiju command.
 * <ul>
 *     <li>Godzilla surfaces offshore and heads for land.</li>
 *     <li>A golden meteor crashes and King Ghidorah emerges.</li>
 *     <li>"Godzilla" walks in from the horizon... but it's Mechagodzilla in disguise.</li>
 * </ul>
 */
@EventBusSubscriber(modid = ReikoGodzilla.MODID)
public final class KaijuEvents {
    public static final List<String> EVENTS = List.of("godzilla", "ghidorah", "mechagodzilla");
    private static final int CHECK_INTERVAL = 1200;

    private KaijuEvents() {}

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) return;
        if (level.getGameTime() % CHECK_INTERVAL != 0 || !GodzillaConfig.get(GodzillaConfig.EVENTS_ENABLED, true)) return;
        if (level.getDayTime() / 24000L < GodzillaConfig.get(GodzillaConfig.EVENT_FIRST_DAY, 5)) return;
        RandomSource random = level.getRandom();
        double rollChance = (double) CHECK_INTERVAL / GodzillaConfig.get(GodzillaConfig.EVENT_INTERVAL_TICKS, 72000);
        if (random.nextDouble() > rollChance || random.nextDouble() > GodzillaConfig.get(GodzillaConfig.EVENT_CHANCE, 0.5)) return;

        List<ServerPlayer> players = level.players().stream().filter(p -> !p.isSpectator() && !p.isCreative()).toList();
        if (players.isEmpty()) return;
        ServerPlayer player = players.get(random.nextInt(players.size()));
        int nearby = level.getEntitiesOfClass(KaijuEntity.class, player.getBoundingBox().inflate(256)).size();
        if (nearby >= GodzillaConfig.get(GodzillaConfig.MAX_KAIJU_NEAR_PLAYER, 2)) return;

        int roll = random.nextInt(100);
        String event1 = roll < 45 ? "godzilla" : roll < 75 ? "ghidorah" : "mechagodzilla";
        if (!trigger(level, player, event1) && !event1.equals("ghidorah")) trigger(level, player, "ghidorah");
    }

    /** Starts an event near the player. Returns false if no suitable spot was found. */
    public static boolean trigger(ServerLevel level, ServerPlayer player, String event) {
        return switch (event) {
            case "godzilla" -> godzillaRises(level, player);
            case "ghidorah" -> ghidorahMeteor(level, player);
            case "mechagodzilla" -> mechagodzillaInvasion(level, player);
            default -> false;
        };
    }

    private static boolean godzillaRises(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < 32; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = 70 + random.nextDouble() * 50;
            int x = (int) (player.getX() + Math.cos(angle) * dist);
            int z = (int) (player.getZ() + Math.sin(angle) * dist);
            BlockPos column = new BlockPos(x, level.getSeaLevel() - 1, z);
            if (!level.hasChunkAt(column) || !level.getBiome(column).is(BiomeTags.IS_OCEAN)) continue;
            int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
            if (level.getSeaLevel() - floor < 8 || !level.getFluidState(column).is(FluidTags.WATER)) continue;

            GodzillaEntity godzilla = ModEntities.GODZILLA.get().create(level);
            if (godzilla == null) return false;
            godzilla.moveTo(x + 0.5, floor, z + 0.5, (float) Math.toDegrees(Math.atan2(player.getZ() - z, player.getX() - x)) - 90f, 0);
            godzilla.finalizeSpawn(level, level.getCurrentDifficultyAt(column), MobSpawnType.EVENT, null);
            godzilla.setDestination(player.blockPosition());
            level.addFreshEntity(godzilla);
            announce(level, player, "event.reikogodzilla.godzilla", ChatFormatting.DARK_AQUA);
            return true;
        }
        return false;
    }

    private static boolean ghidorahMeteor(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.getRandom();
        double angle = random.nextDouble() * Math.PI * 2;
        double dist = 60 + random.nextDouble() * 40;
        double x = player.getX() + Math.cos(angle) * dist;
        double z = player.getZ() + Math.sin(angle) * dist;
        double y = Math.min(level.getMaxBuildHeight() - 10, player.getY() + 140);
        GhidorahMeteorEntity meteor = new GhidorahMeteorEntity(level, x + Math.cos(angle) * 80, y, z + Math.sin(angle) * 80);
        double tx = x - meteor.getX(), tz = z - meteor.getZ();
        double ty = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z) - y;
        double len = Math.sqrt(tx * tx + ty * ty + tz * tz);
        meteor.setDeltaMovement(tx / len * 1.6, ty / len * 1.6, tz / len * 1.6);
        level.addFreshEntity(meteor);
        announce(level, player, "event.reikogodzilla.ghidorah", ChatFormatting.GOLD);
        return true;
    }

    private static boolean mechagodzillaInvasion(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < 32; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = 70 + random.nextDouble() * 40;
            int x = (int) (player.getX() + Math.cos(angle) * dist);
            int z = (int) (player.getZ() + Math.sin(angle) * dist);
            BlockPos column = new BlockPos(x, 0, z);
            if (!level.hasChunkAt(column)) continue;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (!level.getFluidState(new BlockPos(x, y - 1, z)).isEmpty()) continue;
            MechagodzillaEntity mecha = ModEntities.MECHAGODZILLA.get().create(level);
            if (mecha == null) return false;
            mecha.moveTo(x + 0.5, y, z + 0.5, 0, 0);
            mecha.finalizeSpawn(level, level.getCurrentDifficultyAt(column), MobSpawnType.EVENT, null);
            mecha.setDisguised(true);
            mecha.setDestination(player.blockPosition());
            level.addFreshEntity(mecha);
            announce(level, player, "event.reikogodzilla.mechagodzilla", ChatFormatting.GRAY);
            return true;
        }
        return false;
    }

    private static void announce(ServerLevel level, ServerPlayer near, String key, ChatFormatting color) {
        for (ServerPlayer p : level.players()) {
            if (p.distanceToSqr(near) < 512 * 512) p.sendSystemMessage(Component.translatable(key).withStyle(color, ChatFormatting.ITALIC));
        }
    }

    // ------------------------------------------------------------------ /kaiju event <type> [player]

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("kaiju")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("event")
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(EVENTS, builder))
                                .executes(KaijuEvents::runEventCommand))));
    }

    private static int runEventCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String type = StringArgumentType.getString(ctx, "type");
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        if (!EVENTS.contains(type)) {
            ctx.getSource().sendFailure(Component.translatable("commands.reikogodzilla.event.unknown", type));
            return 0;
        }
        if (trigger(player.serverLevel(), player, type)) {
            ctx.getSource().sendSuccess(() -> Component.translatable("commands.reikogodzilla.event.started", type), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.translatable("commands.reikogodzilla.event.no_spot", type));
        return 0;
    }
}

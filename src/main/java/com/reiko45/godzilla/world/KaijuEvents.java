package com.reiko45.godzilla.world;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.reiko45.godzilla.GodzillaConfig;
import com.reiko45.godzilla.ReikoGodzilla;
import com.reiko45.godzilla.entity.KaijuEntity;
import com.reiko45.godzilla.entity.kaiju.GodzillaEntity;
import com.reiko45.godzilla.entity.kaiju.GodzillaJuniorEntity;
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
 *     <li>Burning Godzilla wades ashore, sometimes with Godzilla Junior following him.</li>
 *     <li>A golden meteor crashes and King Ghidorah emerges.</li>
 *     <li>"Godzilla" walks in from the horizon... but it's Mechagodzilla in disguise.</li>
 * </ul>
 */
@EventBusSubscriber(modid = ReikoGodzilla.MODID)
public final class KaijuEvents {
    public static final List<String> EVENTS = List.of("godzilla", "ghidorah", "mechagodzilla", "spacegodzilla", "battra", "destoroyah", "burning_godzilla",
            "monsterverse_godzilla", "muto", "scylla", "behemoth", "alpha_call", "skullcrawlers",
            "gigan", "megalon", "hedorah", "xilien_invasion", "titanosaurus", "orga", "meganulon", "gmk_godzilla", "kamacuras",
            "shin_godzilla", "ginza_raid", "ghidorah_storm", "earth_tremors", "catastrophe", "titan_x", "gigan_invasion", "planet_eater",
            "monarch", "kraken", "supernova");
    /** Relative odds of each random event (same order as {@link #EVENTS}). */
    private static final int[] WEIGHTS = {14, 10, 8, 8, 7, 5, 3, 12, 10, 7, 5, 6, 8, 6, 6, 6, 5, 5, 5, 6, 5, 6, 8, 7, 6, 5, 5, 5, 5, 3, 5, 4, 1};

    /** Delayed spawns (Destoroyah juveniles after an Oxygen Destroyer). Not saved across restarts. */
    private record Pending(net.minecraft.resources.ResourceKey<Level> dimension, BlockPos pos, long time) {}
    private static final java.util.List<Pending> PENDING = new java.util.ArrayList<>();

    public static void scheduleDestoroyah(ServerLevel level, BlockPos pos, int delay) {
        PENDING.add(new Pending(level.dimension(), pos, level.getGameTime() + delay));
    }
    private static final int CHECK_INTERVAL = 1200;

    private KaijuEvents() {}

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!PENDING.isEmpty() && level.getGameTime() % 20 == 0) {
            PENDING.removeIf(p -> {
                if (p.dimension() != level.dimension() || level.getGameTime() < p.time()) return false;
                spawnJuveniles(level, p.pos(), null);
                return true;
            });
        }
        if (level.dimension() != Level.OVERWORLD) return;
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

        int total = 0;
        for (int w : WEIGHTS) total += w;
        int roll = random.nextInt(total);
        String event1 = EVENTS.get(EVENTS.size() - 1);
        for (int i = 0; i < WEIGHTS.length; i++) {
            if (roll < WEIGHTS[i]) {
                event1 = EVENTS.get(i);
                break;
            }
            roll -= WEIGHTS[i];
        }
        if (!trigger(level, player, event1) && !event1.equals("ghidorah")) trigger(level, player, "ghidorah");
    }

    /** Starts an event near the player. Returns false if no suitable spot was found. */
    public static boolean trigger(ServerLevel level, ServerPlayer player, String event) {
        return switch (event) {
            case "godzilla" -> godzillaRises(level, player);
            case "burning_godzilla" -> burningGodzillaRises(level, player);
            case "ghidorah" -> ghidorahMeteor(level, player);
            case "mechagodzilla" -> mechagodzillaInvasion(level, player);
            case "spacegodzilla" -> spaceGodzillaArrives(level, player);
            case "battra" -> battraRises(level, player);
            case "destoroyah" -> spawnJuveniles(level, player.blockPosition(), player);
            case "monsterverse_godzilla" -> MonsterverseEvents.godzilla(level, player);
            case "muto" -> MonsterverseEvents.mutos(level, player);
            case "scylla" -> MonsterverseEvents.scylla(level, player);
            case "behemoth" -> MonsterverseEvents.behemoth(level, player);
            case "alpha_call" -> MonsterverseEvents.alphaCall(level, player);
            case "skullcrawlers" -> MonsterverseEvents.skullcrawlers(level, player);
            case "gigan" -> ClassicEvents.gigan(level, player);
            case "megalon" -> ClassicEvents.megalon(level, player);
            case "hedorah" -> ClassicEvents.hedorah(level, player);
            case "xilien_invasion" -> ClassicEvents.xilienInvasion(level, player);
            case "titanosaurus" -> ClassicEvents.titanosaurus(level, player);
            case "orga" -> ClassicEvents.orga(level, player);
            case "meganulon" -> ClassicEvents.meganulon(level, player);
            case "gmk_godzilla" -> ClassicEvents.gmkGodzilla(level, player);
            case "kamacuras" -> ClassicEvents.kamacuras(level, player);
            case "shin_godzilla" -> ModernEvents.shinGodzilla(level, player);
            case "ginza_raid" -> ModernEvents.ginzaRaid(level, player);
            case "ghidorah_storm" -> ModernEvents.ghidorahStorm(level, player);
            case "earth_tremors" -> ModernEvents.earthTremors(level, player);
            case "catastrophe" -> ModernEvents.catastrophe(level, player);
            case "titan_x" -> ModernEvents.titanX(level, player);
            case "gigan_invasion" -> ModernEvents.giganInvasion(level, player);
            case "planet_eater" -> ModernEvents.planetEater(level, player);
            case "monarch" -> ModernEvents.monarch(level, player);
            case "kraken" -> ModernEvents.kraken(level, player);
            case "supernova" -> ModernEvents.supernova(level, player);
            default -> false;
        };
    }

    private static boolean godzillaRises(ServerLevel level, ServerPlayer player) {
        GodzillaEntity godzilla = riseFromSea(level, player, ModEntities.GODZILLA.get());
        if (godzilla == null) return false;
        announce(level, player, "event.reikogodzilla.godzilla", ChatFormatting.DARK_AQUA);
        return true;
    }

    /** Burning Godzilla wades ashore; half the time Godzilla Junior follows him in. */
    private static boolean burningGodzillaRises(ServerLevel level, ServerPlayer player) {
        GodzillaEntity burning = riseFromSea(level, player, ModEntities.BURNING_GODZILLA.get());
        if (burning == null) return false;
        announce(level, player, "event.reikogodzilla.burning_godzilla", ChatFormatting.GOLD);
        if (level.getRandom().nextBoolean()) {
            GodzillaJuniorEntity junior = ModEntities.GODZILLA_JUNIOR.get().create(level);
            if (junior != null) {
                junior.moveTo(burning.getX() + 12, burning.getY(), burning.getZ() + 12, burning.getYRot(), 0);
                junior.finalizeSpawn(level, level.getCurrentDifficultyAt(junior.blockPosition()), MobSpawnType.EVENT, null);
                junior.setVariant(GodzillaJuniorEntity.JUNIOR);
                junior.setHealth(junior.getMaxHealth());
                level.addFreshEntity(junior);
                announce(level, player, "event.reikogodzilla.burning_godzilla_junior", ChatFormatting.AQUA);
            }
        }
        return true;
    }

    /** Surfaces a Godzilla from the sea 70-120 blocks from the player, heading for them. */
    @javax.annotation.Nullable
    private static <T extends GodzillaEntity> T riseFromSea(ServerLevel level, ServerPlayer player, net.minecraft.world.entity.EntityType<T> type) {
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

            T godzilla = type.create(level);
            if (godzilla == null) return null;
            godzilla.moveTo(x + 0.5, floor, z + 0.5, (float) Math.toDegrees(Math.atan2(player.getZ() - z, player.getX() - x)) - 90f, 0);
            godzilla.finalizeSpawn(level, level.getCurrentDifficultyAt(column), MobSpawnType.EVENT, null);
            godzilla.setDestination(player.blockPosition());
            level.addFreshEntity(godzilla);
            return godzilla;
        }
        return null;
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

    private static boolean spaceGodzillaArrives(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.getRandom();
        double angle = random.nextDouble() * Math.PI * 2;
        double dist = 70 + random.nextDouble() * 40;
        double x = player.getX() + Math.cos(angle) * dist;
        double z = player.getZ() + Math.sin(angle) * dist;
        double y = Math.min(level.getMaxBuildHeight() - 10, player.getY() + 150);
        GhidorahMeteorEntity crystal = new GhidorahMeteorEntity(level, x - Math.cos(angle) * 60, y, z - Math.sin(angle) * 60);
        crystal.setItem(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.AMETHYST_BLOCK));
        crystal.setPayload(ModEntities.SPACE_GODZILLA.get());
        double tx = x - crystal.getX(), tz = z - crystal.getZ();
        double ty = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z) - y;
        double len = Math.sqrt(tx * tx + ty * ty + tz * tz);
        crystal.setDeltaMovement(tx / len * 1.8, ty / len * 1.8, tz / len * 1.8);
        level.addFreshEntity(crystal);
        announce(level, player, "event.reikogodzilla.spacegodzilla", ChatFormatting.LIGHT_PURPLE);
        return true;
    }

    private static boolean battraRises(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < 32; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = 60 + random.nextDouble() * 40;
            int x = (int) (player.getX() + Math.cos(angle) * dist);
            int z = (int) (player.getZ() + Math.sin(angle) * dist);
            BlockPos column = new BlockPos(x, 0, z);
            if (!level.hasChunkAt(column)) continue;
            int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
            var larva = ModEntities.BATTRA_LARVA.get().create(level);
            if (larva == null) return false;
            larva.moveTo(x + 0.5, y, z + 0.5, 0, 0);
            larva.finalizeSpawn(level, level.getCurrentDifficultyAt(column), MobSpawnType.EVENT, null);
            larva.setDestination(player.blockPosition());
            level.addFreshEntity(larva);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER, x, y + 2, z, 1, 0, 0, 0, 0);
            announce(level, player, "event.reikogodzilla.battra", ChatFormatting.DARK_RED);
            return true;
        }
        return false;
    }

    /** A swarm of Destoroyah juveniles crawls ashore near {@code pos}. */
    private static boolean spawnJuveniles(ServerLevel level, BlockPos pos, @javax.annotation.Nullable ServerPlayer near) {
        RandomSource random = level.getRandom();
        BlockPos land = null;
        for (int attempt = 0; attempt < 40 && land == null; attempt++) {
            int x = pos.getX() + random.nextInt(65) - 32 + (near != null ? (random.nextBoolean() ? 20 : -20) : 0);
            int z = pos.getZ() + random.nextInt(65) - 32;
            if (!level.hasChunkAt(new BlockPos(x, 0, z))) continue;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos candidate = new BlockPos(x, y, z);
            if (level.getFluidState(candidate.below()).isEmpty()) land = candidate;
        }
        if (land == null) return false;
        int count = 6 + random.nextInt(3);
        for (int i = 0; i < count; i++) {
            var juvenile = ModEntities.DESTOROYAH_JUVENILE.get().create(level);
            if (juvenile == null) continue;
            BlockPos p = land.offset(random.nextInt(7) - 3, 0, random.nextInt(7) - 3);
            p = new BlockPos(p.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, p.getX(), p.getZ()), p.getZ());
            juvenile.moveTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, random.nextFloat() * 360f, 0);
            juvenile.finalizeSpawn(level, level.getCurrentDifficultyAt(p), MobSpawnType.EVENT, null);
            level.addFreshEntity(juvenile);
        }
        for (ServerPlayer p : level.players()) {
            if (p.distanceToSqr(land.getX(), land.getY(), land.getZ()) < 256 * 256) {
                p.sendSystemMessage(Component.translatable("event.reikogodzilla.destoroyah").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
            }
        }
        return true;
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
        if (type.equals("supernova") && !GodzillaConfig.get(GodzillaConfig.SUPERNOVA_PLACEHOLDER, false)) {
            ctx.getSource().sendFailure(Component.translatable("commands.reikogodzilla.event.supernova_disabled"));
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

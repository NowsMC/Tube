package space.nows.tube.server.gameplay;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerRespawnEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import space.nows.tube.api.world.Environment;
import space.nows.tube.api.world.Location;
import space.nows.tube.server.player.RespawnPoint;
import space.nows.tube.server.world.MinestomWorld;
import space.nows.tube.server.world.WorldService;

import java.util.Set;

final class Beds {

    private static final Set<EntityType> MONSTERS = Set.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.SPIDER,
            EntityType.HUSK, EntityType.STRAY, EntityType.DROWNED, EntityType.WITCH, EntityType.ENDERMAN, EntityType.ZOMBIE_VILLAGER);
    private static final int SLEEP_TICKS = 100;

    private final WorldService worlds;

    Beds(WorldService worlds) {
        this.worlds = worlds;
    }

    static boolean isNight(Instance instance) {
        long time = Math.floorMod(instance.getTime(), 24000L);
        return time >= 12542 && time < 23460;
    }

    static boolean isSleeping(Player player) {
        var meta = player.getLivingEntityMeta();
        return meta != null && meta.getBedInWhichSleepingPosition() != null;
    }

    void install(GlobalEventHandler handler) {
        handler.addListener(PlayerSpawnEvent.class, event -> {
            if (event.isFirstSpawn()) {
                Location spawn = worlds.defaultWorld().spawn();
                event.getPlayer().setRespawnPoint(new Pos(spawn.x(), spawn.y(), spawn.z()));
            }
        });
        handler.addListener(PlayerBlockInteractEvent.class, this::useBed);
        handler.addListener(PlayerRespawnEvent.class, this::respawn);
        handler.addListener(EntityDamageEvent.class, event -> {
            if (event.getEntity() instanceof Player player && isSleeping(player)) {
                player.leaveBed();
            }
        });
        MinecraftServer.getSchedulerManager().buildTask(this::tickSleep).repeat(TaskSchedule.tick(1)).schedule();
    }

    private void useBed(PlayerBlockInteractEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (event.isCancelled() || !block.key().value().endsWith("_bed") || player.isSneaking() && !player.getItemInMainHand().isAir()) {
            return;
        }
        event.setBlockingItemUse(true);
        Instance instance = event.getInstance();
        MinestomWorld world = worlds.of(instance);
        Point head = event.getBlockPosition();
        if ("foot".equals(block.getProperty("part"))) {
            head = switch (block.getProperty("facing")) {
                case "north" -> head.add(0, 0, -1);
                case "south" -> head.add(0, 0, 1);
                case "east" -> head.add(1, 0, 0);
                default -> head.add(-1, 0, 0);
            };
        }
        if (world.options().environment() != Environment.OVERWORLD) {
            instance.setBlock(event.getBlockPosition(), Block.AIR);
            Explosions.explode(instance, head.add(0.5, 0.5, 0.5), 5, true, null);
            return;
        }
        RespawnPoint current = player.getTag(RespawnPoint.TAG);
        RespawnPoint point = new RespawnPoint(world.name(), new Pos(head.blockX(), head.blockY(), head.blockZ()));
        if (!point.equals(current)) {
            player.setTag(RespawnPoint.TAG, point);
            player.sendMessage(Component.translatable("block.minecraft.set_spawn"));
        }
        if (!isNight(instance)) {
            player.sendActionBar(Component.translatable("block.minecraft.bed.no_sleep"));
            return;
        }
        if (player.getPosition().distanceSquared(head.add(0.5, 0.5, 0.5)) > 9) {
            player.sendActionBar(Component.translatable("block.minecraft.bed.too_far_away"));
            return;
        }
        for (Entity nearby : instance.getNearbyEntities(head, 8)) {
            if (MONSTERS.contains(nearby.getEntityType())) {
                player.sendActionBar(Component.translatable("block.minecraft.bed.not_safe"));
                return;
            }
        }
        player.teleport(new Pos(head.blockX() + 0.5, head.blockY() + 0.5625, head.blockZ() + 0.5, player.getPosition().yaw(), 0));
        player.enterBed(head);
        player.setTag(SleepTimer.TAG, 0);
    }

    private static final class SleepTimer {
        static final net.minestom.server.tag.Tag<Integer> TAG = net.minestom.server.tag.Tag.Transient("tube:sleep_ticks");
    }

    private void tickSleep() {
        for (Instance instance : MinecraftServer.getInstanceManager().getInstances()) {
            var players = instance.getPlayers();
            if (players.isEmpty()) {
                continue;
            }
            boolean anySleeping = false;
            boolean allReady = true;
            for (Player player : players) {
                if (player.getGameMode() == GameMode.SPECTATOR) {
                    continue;
                }
                if (isSleeping(player)) {
                    anySleeping = true;
                    int ticks = player.getTag(SleepTimer.TAG) == null ? 0 : player.getTag(SleepTimer.TAG) + 1;
                    player.setTag(SleepTimer.TAG, ticks);
                    allReady &= ticks >= SLEEP_TICKS;
                } else {
                    allReady = false;
                }
            }
            if (anySleeping && allReady) {
                long time = instance.getTime();
                instance.setTime(time + (24000 - Math.floorMod(time, 24000L)));
                for (Player player : players) {
                    if (isSleeping(player)) {
                        player.leaveBed();
                        player.removeTag(SleepTimer.TAG);
                    }
                }
            }
        }
    }

    private void respawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!event.getRespawnPosition().samePoint(player.getRespawnPoint())) {
            return;
        }
        Location spawn = worlds.defaultWorld().spawn();
        Instance target = worlds.instanceOf(spawn.world());
        Pos position = new Pos(spawn.x(), spawn.y(), spawn.z());
        RespawnPoint bed = player.getTag(RespawnPoint.TAG);
        if (bed != null) {
            var world = worlds.loadExisting(bed.world());
            if (world.isPresent()) {
                Instance bedInstance = worlds.instanceOf(world.get());
                bedInstance.loadChunk(bed.position()).join();
                if (bedInstance.getBlock(bed.position()).key().value().endsWith("_bed")) {
                    target = bedInstance;
                    position = bed.position().add(0.5, 0.5625, 0.5);
                } else {
                    player.removeTag(RespawnPoint.TAG);
                    player.sendMessage(Component.translatable("block.minecraft.spawn.not_valid"));
                }
            }
        }
        event.setRespawnPosition(position);
        if (target != player.getInstance()) {
            Instance destination = target;
            Pos at = position;
            player.scheduleNextTick(ignored -> player.setInstance(destination, at));
        }
    }
}

package space.nows.tube.bukkit;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import space.nows.tube.bukkit.command.BukkitCommandSender;
import space.nows.tube.bukkit.command.BukkitConsoleSender;
import space.nows.tube.bukkit.entity.BukkitEntity;
import space.nows.tube.bukkit.entity.BukkitItem;
import space.nows.tube.bukkit.entity.BukkitMob;
import space.nows.tube.bukkit.entity.BukkitOfflinePlayer;
import space.nows.tube.bukkit.entity.BukkitPlayer;
import space.nows.tube.bukkit.world.BukkitWorld;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Adapters {

    private final BukkitRuntime runtime;
    private final Map<UUID, BukkitPlayer> players = new ConcurrentHashMap<>();
    private final Map<String, BukkitWorld> worlds = new ConcurrentHashMap<>();
    private final BukkitConsoleSender console;

    Adapters(BukkitRuntime runtime) {
        this.runtime = runtime;
        this.console = new BukkitConsoleSender(runtime);
    }

    public ConsoleCommandSender console() {
        return console;
    }

    public CommandSender sender(space.nows.tube.api.command.CommandSender sender) {
        if (sender instanceof space.nows.tube.api.entity.player.Player player) {
            return player(player);
        }
        return new BukkitCommandSender(runtime, sender);
    }

    public Player player(space.nows.tube.api.entity.player.Player tube) {
        return players.computeIfAbsent(tube.uuid(), uuid -> new BukkitPlayer(runtime, tube));
    }

    public void forget(UUID uuid) {
        players.remove(uuid);
    }

    public LivingEntity living(space.nows.tube.api.entity.LivingEntity tube) {
        if (tube instanceof space.nows.tube.api.entity.player.Player player) {
            return player(player);
        }
        return new BukkitMob(runtime, tube);
    }

    public org.bukkit.entity.Entity entity(space.nows.tube.api.entity.Entity tube) {
        return switch (tube) {
            case space.nows.tube.api.entity.LivingEntity living -> living(living);
            case space.nows.tube.api.entity.ItemEntity item -> item(item);
            default -> new BukkitEntity(runtime, tube);
        };
    }

    public Item item(space.nows.tube.api.entity.ItemEntity tube) {
        return new BukkitItem(runtime, tube);
    }

    public void forgetWorld(String name) {
        worlds.remove(name);
    }

    public World world(space.nows.tube.api.world.World tube) {
        return worlds.computeIfAbsent(tube.name(), name -> new BukkitWorld(runtime, tube));
    }

    public List<Player> onlinePlayers() {
        return runtime.tube().players().stream().map(this::player).toList();
    }

    public OfflinePlayer offline(UUID uuid, String name) {
        return new BukkitOfflinePlayer(runtime, uuid, name);
    }

    public Location location(space.nows.tube.api.world.Location tube) {
        return new Location(world(tube.world()), tube.x(), tube.y(), tube.z(), tube.yaw(), tube.pitch());
    }

    public space.nows.tube.api.world.Location tubeLocation(Location location) {
        BukkitWorld world = (BukkitWorld) location.getWorld();
        return new space.nows.tube.api.world.Location(world.tube(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public static UUID offlineUuid(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }
}

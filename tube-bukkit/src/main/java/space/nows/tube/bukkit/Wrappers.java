package space.nows.tube.bukkit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

final class Wrappers {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static final Map<Object, Object> BACKING = Collections.synchronizedMap(new WeakHashMap<>());

    private final BukkitRuntime runtime;
    private final Map<UUID, Player> players = new ConcurrentHashMap<>();
    private final Map<String, World> worlds = new ConcurrentHashMap<>();
    private volatile ConsoleCommandSender console;

    Wrappers(BukkitRuntime runtime) {
        this.runtime = runtime;
    }

    static <T> T unwrap(Object proxy, Class<T> type) {
        Object target = BACKING.get(proxy);
        if (target == null) {
            throw new IllegalArgumentException("Not a Tube-backed object: " + proxy);
        }
        return type.cast(target);
    }

    private static <T> T backed(T proxy, Object target) {
        BACKING.put(proxy, target);
        return proxy;
    }

    ConsoleCommandSender console() {
        if (console == null) {
            synchronized (this) {
                if (console == null) {
                    console = consoleFor(consoleSender());
                }
            }
        }
        return console;
    }

    private ConsoleCommandSender consoleFor(space.nows.tube.api.CommandSender backing) {
        return backed(Proxies.of(ConsoleCommandSender.class, "ConsoleCommandSender")
                .on("getName", args -> "CONSOLE")
                .on("isOp", args -> true)
                .on("hasPermission", args -> true)
                .on("isPermissionSet", args -> true)
                .on("getServer", args -> runtime.server)
                .on("sendMessage", args -> {
                    send(backing, args);
                    return null;
                })
                .build(), backing);
    }

    private CommandSender genericSender(space.nows.tube.api.CommandSender backing) {
        return backed(Proxies.of(CommandSender.class, "CommandSender(" + backing.name() + ")")
                .on("getName", args -> backing.name())
                .on("isOp", args -> false)
                .on("hasPermission", args -> args[0] instanceof Permission permission ? backing.hasPermission(permission.getName()) : backing.hasPermission(String.valueOf(args[0])))
                .on("isPermissionSet", args -> true)
                .on("getServer", args -> runtime.server)
                .on("sendMessage", args -> {
                    send(backing, args);
                    return null;
                })
                .build(), backing);
    }

    CommandSender sender(space.nows.tube.api.CommandSender sender) {
        if (sender instanceof space.nows.tube.api.Player player) {
            return player(player);
        }
        return sender.name().equals("Console") ? consoleFor(sender) : genericSender(sender);
    }

    Player player(space.nows.tube.api.Player tube) {
        return players.computeIfAbsent(tube.uuid(), uuid -> createPlayer(tube));
    }

    void forget(UUID uuid) {
        players.remove(uuid);
    }

    World world(space.nows.tube.api.World tube) {
        return worlds.computeIfAbsent(tube.name(), name -> createWorld(tube));
    }

    List<Player> onlinePlayers() {
        return runtime.tube.players().stream().map(this::player).toList();
    }

    Location location(space.nows.tube.api.Location tube) {
        return new Location(world(tube.world()), tube.x(), tube.y(), tube.z(), tube.yaw(), tube.pitch());
    }

    space.nows.tube.api.Location tubeLocation(Location location) {
        return new space.nows.tube.api.Location(unwrap(location.getWorld(), space.nows.tube.api.World.class),
                location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    private static void send(space.nows.tube.api.CommandSender target, Object[] args) {
        Object message = args.length == 2 && args[0] instanceof UUID ? args[1] : args[args.length - 1];
        if (message instanceof String text) {
            target.sendMessage(LEGACY.deserialize(text));
        } else if (message instanceof String[] lines) {
            for (String line : lines) {
                target.sendMessage(LEGACY.deserialize(line));
            }
        }
    }

    private static space.nows.tube.api.CommandSender consoleSender() {
        return new space.nows.tube.api.CommandSender() {
            @Override
            public String name() {
                return "Console";
            }

            @Override
            public boolean hasPermission(String node) {
                return true;
            }

            @Override
            public void sendMessage(Component message) {
                org.slf4j.LoggerFactory.getLogger("tube").info(PlainTextComponentSerializer.plainText().serialize(message));
            }
        };
    }

    boolean hasPermission(space.nows.tube.api.Player tube, Object permission) {
        String node = permission instanceof Permission p ? p.getName() : String.valueOf(permission);
        if (tube.hasPermission(node)) {
            return true;
        }
        Permission registered = runtime.pluginManager.getPermission(node);
        if (registered == null) {
            return false;
        }
        PermissionDefault fallback = registered.getDefault();
        return fallback.getValue(runtime.tube.permissions().isOp(tube.uuid()));
    }

    private Player createPlayer(space.nows.tube.api.Player tube) {
        return backed(Proxies.of(Player.class, "Player(" + tube.name() + ")")
                .on("getName", args -> tube.name())
                .on("getUniqueId", args -> tube.uuid())
                .on("getDisplayName", args -> tube.name())
                .on("getPlayerListName", args -> tube.name())
                .on("getPlayer", args -> runtime.wrappers.player(tube))
                .on("isOnline", args -> true)
                .on("isValid", args -> !tube.isRemoved())
                .on("isDead", args -> tube.health() <= 0)
                .on("getServer", args -> runtime.server)
                .on("getType", args -> EntityType.PLAYER)
                .on("getEntityId", args -> tube.uuid().hashCode())
                .on("isOp", args -> runtime.tube.permissions().isOp(tube.uuid()))
                .on("setOp", args -> {
                    runtime.tube.permissions().setOp(tube.uuid(), tube.name(), (Boolean) args[0]);
                    return null;
                })
                .on("hasPermission", args -> hasPermission(tube, args[0]))
                .on("isPermissionSet", args -> hasPermission(tube, args[0]))
                .on("sendMessage", args -> {
                    send(tube, args);
                    return null;
                })
                .on("getWorld", args -> world(tube.world()))
                .on("getLocation/0", args -> location(tube.location()))
                .on("teleport", args -> {
                    if (args[0] instanceof Location location) {
                        tube.teleport(tubeLocation(location));
                        return true;
                    }
                    return false;
                })
                .on("getHealth", args -> tube.health())
                .on("setHealth", args -> {
                    tube.setHealth((Double) args[0]);
                    return null;
                })
                .on("getMaxHealth", args -> tube.maxHealth())
                .on("getFoodLevel", args -> tube.food())
                .on("setFoodLevel", args -> {
                    tube.setFood((Integer) args[0]);
                    return null;
                })
                .on("getLevel", args -> tube.level())
                .on("setLevel", args -> {
                    tube.setLevel((Integer) args[0]);
                    return null;
                })
                .on("getGameMode", args -> GameMode.valueOf(tube.gameMode().name()))
                .on("setGameMode", args -> {
                    tube.setGameMode(space.nows.tube.api.GameMode.valueOf(((GameMode) args[0]).name()));
                    return null;
                })
                .on("kickPlayer", args -> {
                    tube.kick(args[0] == null ? "" : String.valueOf(args[0]));
                    return null;
                })
                .on("getAddress", args -> tube.address().orElse((InetSocketAddress) null))
                .on("performCommand", args -> runtime.commandMap.dispatch(runtime.wrappers.player(tube), (String) args[0]))
                .on("equals", args -> args[0] != null && BACKING.get(args[0]) instanceof space.nows.tube.api.Player other && other.uuid().equals(tube.uuid()))
                .on("hashCode", args -> tube.uuid().hashCode())
                .on("toString", args -> "CraftPlayer{name=" + tube.name() + "}")
                .build(), tube);
    }

    private World createWorld(space.nows.tube.api.World tube) {
        return backed(Proxies.of(World.class, "World(" + tube.name() + ")")
                .on("getName", args -> tube.name())
                .on("getUID", args -> UUID.nameUUIDFromBytes(("tube:world:" + tube.name()).getBytes()))
                .on("getEnvironment", args -> World.Environment.NORMAL)
                .on("getSpawnLocation", args -> location(tube.spawn()))
                .on("getMinHeight", args -> -64)
                .on("getMaxHeight", args -> 320)
                .on("removePluginChunkTickets", args -> null)
                .on("getSeaLevel", args -> 63)
                .on("getBlockAt/3", args -> block(tube, (Integer) args[0], (Integer) args[1], (Integer) args[2]))
                .on("getBlockAt/1", args -> {
                    Location location = (Location) args[0];
                    return block(tube, location.getBlockX(), location.getBlockY(), location.getBlockZ());
                })
                .on("getPlayers", args -> runtime.tube.players().stream()
                        .filter(player -> player.world().equals(tube))
                        .map(this::player)
                        .toList())
                .on("save", args -> {
                    tube.save();
                    return null;
                })
                .on("equals", args -> args[0] != null && BACKING.get(args[0]) instanceof space.nows.tube.api.World other && other.name().equals(tube.name()))
                .on("hashCode", args -> tube.name().hashCode())
                .on("toString", args -> "CraftWorld{name=" + tube.name() + "}")
                .build(), tube);
    }

    private Block block(space.nows.tube.api.World tube, int x, int y, int z) {
        World world = world(tube);
        return Proxies.of(Block.class, "Block")
                .on("getX", args -> x)
                .on("getY", args -> y)
                .on("getZ", args -> z)
                .on("getWorld", args -> world)
                .on("getLocation/0", args -> new Location(world, x, y, z))
                .on("getType", args -> materialOf(tube.block(x, y, z)))
                .on("setType", args -> {
                    tube.setBlock(x, y, z, space.nows.tube.api.Block.of(((Material) args[0]).getKey().toString()));
                    return null;
                })
                .on("isEmpty", args -> tube.block(x, y, z).isAir())
                .on("isLiquid", args -> false)
                .on("equals", args -> args[0] instanceof Block other && other.getX() == x && other.getY() == y && other.getZ() == z && world.equals(other.getWorld()))
                .on("hashCode", args -> java.util.Objects.hash(x, y, z))
                .build();
    }

    private static Material materialOf(space.nows.tube.api.Block block) {
        Material material = Material.matchMaterial(block.key());
        return material == null ? Material.AIR : material;
    }
}

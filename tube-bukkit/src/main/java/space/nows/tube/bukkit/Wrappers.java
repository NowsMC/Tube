package space.nows.tube.bukkit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class Wrappers {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private final BukkitRuntime runtime;
    private final Map<UUID, BukkitPlayer> players = new ConcurrentHashMap<>();
    private final Map<String, BukkitWorld> worlds = new ConcurrentHashMap<>();
    private final BukkitConsole console;

    Wrappers(BukkitRuntime runtime) {
        this.runtime = runtime;
        this.console = new BukkitConsole(runtime);
    }

    ConsoleCommandSender console() {
        return console;
    }

    CommandSender sender(space.nows.tube.api.CommandSender sender) {
        if (sender instanceof space.nows.tube.api.Player player) {
            return player(player);
        }
        return new BukkitSender(runtime, sender);
    }

    Player player(space.nows.tube.api.Player tube) {
        return players.computeIfAbsent(tube.uuid(), uuid -> new BukkitPlayer(runtime, tube));
    }

    void forget(UUID uuid) {
        players.remove(uuid);
    }

    org.bukkit.entity.LivingEntity living(space.nows.tube.api.LivingEntity tube) {
        if (tube instanceof space.nows.tube.api.Player player) {
            return player(player);
        }
        return new BukkitMob(runtime, tube);
    }

    World world(space.nows.tube.api.World tube) {
        return worlds.computeIfAbsent(tube.name(), name -> new BukkitWorld(runtime, tube));
    }

    List<Player> onlinePlayers() {
        return runtime.tube.players().stream().map(this::player).toList();
    }

    OfflinePlayer offline(UUID uuid, String name) {
        return new BukkitOfflinePlayer(runtime, uuid, name);
    }

    Location location(space.nows.tube.api.Location tube) {
        return new Location(world(tube.world()), tube.x(), tube.y(), tube.z(), tube.yaw(), tube.pitch());
    }

    space.nows.tube.api.Location tubeLocation(Location location) {
        BukkitWorld world = (BukkitWorld) location.getWorld();
        return new space.nows.tube.api.Location(world.tube, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    static UUID offlineUuid(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }

    private static void deliver(space.nows.tube.api.CommandSender target, String message) {
        target.sendMessage(LEGACY.deserialize(message));
    }

    private static final class BukkitConsole extends BukkitPermissible implements ConsoleCommandSender {

        BukkitConsole(BukkitRuntime runtime) {
            super(runtime);
        }

        @Override
        boolean granted(String node) {
            return true;
        }

        @Override
        public boolean isOp() {
            return true;
        }

        @Override
        public void setOp(boolean value) {
        }

        @Override
        public void sendMessage(String message) {
            org.slf4j.LoggerFactory.getLogger("tube").info(PlainTextComponentSerializer.plainText().serialize(LEGACY.deserialize(message)));
        }

        @Override
        public void sendMessage(String... messages) {
            for (String message : messages) {
                sendMessage(message);
            }
        }

        @Override
        public Server getServer() {
            return runtime.server;
        }

        @Override
        public String getName() {
            return "CONSOLE";
        }
    }

    private static final class BukkitSender extends BukkitPermissible implements CommandSender {

        private final space.nows.tube.api.CommandSender tube;

        BukkitSender(BukkitRuntime runtime, space.nows.tube.api.CommandSender tube) {
            super(runtime);
            this.tube = tube;
        }

        @Override
        boolean granted(String node) {
            return tube.hasPermission(node);
        }

        @Override
        public boolean isOp() {
            return tube instanceof ConsoleCommandSender;
        }

        @Override
        public void setOp(boolean value) {
        }

        @Override
        public void sendMessage(String message) {
            deliver(tube, message);
        }

        @Override
        public void sendMessage(String... messages) {
            for (String message : messages) {
                deliver(tube, message);
            }
        }

        @Override
        public Server getServer() {
            return runtime.server;
        }

        @Override
        public String getName() {
            return tube.name();
        }
    }

    private static final class BukkitOfflinePlayer implements OfflinePlayer {

        private final BukkitRuntime runtime;
        private final UUID uuid;
        private final String name;

        BukkitOfflinePlayer(BukkitRuntime runtime, UUID uuid, String name) {
            this.runtime = runtime;
            this.uuid = uuid;
            this.name = name;
        }

        @Override
        public boolean isOnline() {
            return getPlayer() != null;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public UUID getUniqueId() {
            return uuid;
        }

        @Override
        public boolean isBanned() {
            return false;
        }

        @Override
        public boolean isWhitelisted() {
            return false;
        }

        @Override
        public void setWhitelisted(boolean whitelisted) {
        }

        @Override
        public Player getPlayer() {
            return runtime.server.getPlayer(uuid);
        }

        @Override
        public long getFirstPlayed() {
            return 0;
        }

        @Override
        public long getLastPlayed() {
            return 0;
        }

        @Override
        public boolean hasPlayedBefore() {
            return false;
        }

        @Override
        public boolean isOp() {
            return runtime.tube.permissions().isOp(uuid);
        }

        @Override
        public void setOp(boolean value) {
            runtime.tube.permissions().setOp(uuid, name == null ? uuid.toString() : name, value);
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof OfflinePlayer player && uuid.equals(player.getUniqueId());
        }

        @Override
        public int hashCode() {
            return uuid.hashCode();
        }
    }

    private static final class BukkitPlayer extends BukkitPermissible implements Player {

        private final space.nows.tube.api.Player tube;

        BukkitPlayer(BukkitRuntime runtime, space.nows.tube.api.Player tube) {
            super(runtime);
            this.tube = tube;
        }

        @Override
        boolean granted(String node) {
            return tube.hasPermission(node);
        }

        @Override
        public String getName() {
            return tube.name();
        }

        @Override
        public String getDisplayName() {
            return tube.name();
        }

        @Override
        public String getPlayerListName() {
            return tube.name();
        }

        @Override
        public UUID getUniqueId() {
            return tube.uuid();
        }

        @Override
        public boolean isOnline() {
            return !tube.isRemoved();
        }

        @Override
        public Player getPlayer() {
            return this;
        }

        @Override
        public Server getServer() {
            return runtime.server;
        }

        @Override
        public int getEntityId() {
            return tube.uuid().hashCode();
        }

        @Override
        public EntityType getType() {
            return EntityType.PLAYER;
        }

        @Override
        public Location getLocation() {
            return runtime.wrappers.location(tube.location());
        }

        @Override
        public World getWorld() {
            return runtime.wrappers.world(tube.world());
        }

        @Override
        public boolean teleport(Location location) {
            tube.teleport(runtime.wrappers.tubeLocation(location));
            return true;
        }

        @Override
        public String getCustomName() {
            return tube.customName().map(component -> LEGACY.serialize(component)).orElse(null);
        }

        @Override
        public void setCustomName(String name) {
            tube.setCustomName(name == null ? null : LEGACY.deserialize(name));
        }

        @Override
        public boolean isValid() {
            return !tube.isRemoved();
        }

        @Override
        public boolean isDead() {
            return tube.health() <= 0;
        }

        @Override
        public void remove() {
            tube.remove();
        }

        @Override
        public double getHealth() {
            return tube.health();
        }

        @Override
        public void setHealth(double health) {
            tube.setHealth(health);
        }

        @Override
        public double getMaxHealth() {
            return tube.maxHealth();
        }

        @Override
        public GameMode getGameMode() {
            return GameMode.valueOf(tube.gameMode().name());
        }

        @Override
        public void setGameMode(GameMode mode) {
            tube.setGameMode(space.nows.tube.api.GameMode.valueOf(mode.name()));
        }

        @Override
        public int getFoodLevel() {
            return tube.food();
        }

        @Override
        public void setFoodLevel(int level) {
            tube.setFood(level);
        }

        @Override
        public int getLevel() {
            return tube.level();
        }

        @Override
        public void setLevel(int level) {
            tube.setLevel(level);
        }

        @Override
        public org.bukkit.inventory.PlayerInventory getInventory() {
            return new BukkitInventory.OfPlayer(tube, this);
        }

        @Override
        public org.bukkit.inventory.ItemStack getItemInHand() {
            return getInventory().getItemInMainHand();
        }

        @Override
        public void setItemInHand(org.bukkit.inventory.ItemStack item) {
            getInventory().setItemInMainHand(item);
        }

        @Override
        public int getPing() {
            return tube.latency();
        }

        @Override
        public void sendPluginMessage(org.bukkit.plugin.Plugin source, String channel, byte[] message) {
            runtime.messenger.send(source, tube, channel, message);
        }

        @Override
        public java.util.Set<String> getListeningPluginChannels() {
            return runtime.messenger.listening();
        }

        @Override
        public InetSocketAddress getAddress() {
            return tube.address().orElse(null);
        }

        @Override
        public void kickPlayer(String message) {
            tube.kick(message == null ? "" : message);
        }

        @Override
        public boolean performCommand(String command) {
            return runtime.commands.dispatch(this, command);
        }

        @Override
        public boolean isOp() {
            return runtime.tube.permissions().isOp(tube.uuid());
        }

        @Override
        public void setOp(boolean value) {
            runtime.tube.permissions().setOp(tube.uuid(), tube.name(), value);
        }

        @Override
        public boolean isBanned() {
            return false;
        }

        @Override
        public boolean isWhitelisted() {
            return false;
        }

        @Override
        public void setWhitelisted(boolean whitelisted) {
        }

        @Override
        public long getFirstPlayed() {
            return 0;
        }

        @Override
        public long getLastPlayed() {
            return System.currentTimeMillis();
        }

        @Override
        public boolean hasPlayedBefore() {
            return true;
        }

        @Override
        public void sendMessage(String message) {
            deliver(tube, message);
        }

        @Override
        public void sendMessage(String... messages) {
            for (String message : messages) {
                deliver(tube, message);
            }
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Player player && player.getUniqueId().equals(tube.uuid());
        }

        @Override
        public int hashCode() {
            return tube.uuid().hashCode();
        }

        @Override
        public String toString() {
            return "CraftPlayer{name=" + tube.name() + "}";
        }
    }

    private static final class BukkitMob extends BukkitPermissible implements org.bukkit.entity.LivingEntity {

        private final space.nows.tube.api.LivingEntity tube;

        BukkitMob(BukkitRuntime runtime, space.nows.tube.api.LivingEntity tube) {
            super(runtime);
            this.tube = tube;
        }

        @Override
        boolean granted(String node) {
            return false;
        }

        @Override
        public boolean isOp() {
            return false;
        }

        @Override
        public void setOp(boolean value) {
        }

        @Override
        public void sendMessage(String message) {
        }

        @Override
        public void sendMessage(String... messages) {
        }

        @Override
        public String getName() {
            return tube.customName().map(component -> LEGACY.serialize(component)).orElse(tube.type());
        }

        @Override
        public Server getServer() {
            return runtime.server;
        }

        @Override
        public Location getLocation() {
            return runtime.wrappers.location(tube.location());
        }

        @Override
        public int getEntityId() {
            return tube.uuid().hashCode();
        }

        @Override
        public World getWorld() {
            return runtime.wrappers.world(tube.world());
        }

        @Override
        public EntityType getType() {
            EntityType type = EntityType.fromName(tube.type());
            return type == null ? EntityType.UNKNOWN : type;
        }

        @Override
        public UUID getUniqueId() {
            return tube.uuid();
        }

        @Override
        public boolean teleport(Location location) {
            tube.teleport(runtime.wrappers.tubeLocation(location));
            return true;
        }

        @Override
        public String getCustomName() {
            return tube.customName().map(component -> LEGACY.serialize(component)).orElse(null);
        }

        @Override
        public void setCustomName(String name) {
            tube.setCustomName(name == null ? null : LEGACY.deserialize(name));
        }

        @Override
        public boolean isValid() {
            return !tube.isRemoved();
        }

        @Override
        public boolean isDead() {
            return tube.health() <= 0;
        }

        @Override
        public void remove() {
            tube.remove();
        }

        @Override
        public double getHealth() {
            return tube.health();
        }

        @Override
        public void setHealth(double health) {
            tube.setHealth(health);
        }

        @Override
        public double getMaxHealth() {
            return tube.maxHealth();
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof org.bukkit.entity.Entity entity && entity.getUniqueId().equals(tube.uuid());
        }

        @Override
        public int hashCode() {
            return tube.uuid().hashCode();
        }
    }

    private static final class BukkitWorld implements World {

        private final BukkitRuntime runtime;
        private final space.nows.tube.api.World tube;

        BukkitWorld(BukkitRuntime runtime, space.nows.tube.api.World tube) {
            this.runtime = runtime;
            this.tube = tube;
        }

        @Override
        public String getName() {
            return tube.name();
        }

        @Override
        public UUID getUID() {
            return UUID.nameUUIDFromBytes(("tube:world:" + tube.name()).getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public NamespacedKey getKey() {
            return NamespacedKey.minecraft(tube.name().toLowerCase(java.util.Locale.ROOT));
        }

        @Override
        public Location getSpawnLocation() {
            return runtime.wrappers.location(tube.spawn());
        }

        @Override
        public Environment getEnvironment() {
            return Environment.NORMAL;
        }

        @Override
        public Block getBlockAt(int x, int y, int z) {
            return new BukkitBlock(runtime, this, x, y, z);
        }

        @Override
        public Block getBlockAt(Location location) {
            return getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        }

        @Override
        public List<Player> getPlayers() {
            return runtime.tube.players().stream()
                    .filter(player -> player.world().name().equals(tube.name()))
                    .map(runtime.wrappers::player)
                    .toList();
        }

        @Override
        public List<Entity> getEntities() {
            return List.copyOf(getPlayers());
        }

        @Override
        public int getMinHeight() {
            return -64;
        }

        @Override
        public int getMaxHeight() {
            return 320;
        }

        @Override
        public int getSeaLevel() {
            return 63;
        }

        @Override
        public void save() {
            tube.save();
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof BukkitWorld world && world.tube.name().equals(tube.name());
        }

        @Override
        public int hashCode() {
            return tube.name().hashCode();
        }

        @Override
        public String toString() {
            return "CraftWorld{name=" + tube.name() + "}";
        }
    }

    private static final class BukkitBlock implements Block {

        private final BukkitRuntime runtime;
        private final BukkitWorld world;
        private final int x;
        private final int y;
        private final int z;

        BukkitBlock(BukkitRuntime runtime, BukkitWorld world, int x, int y, int z) {
            this.runtime = runtime;
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public int getZ() {
            return z;
        }

        @Override
        public World getWorld() {
            return world;
        }

        @Override
        public Location getLocation() {
            return new Location(world, x, y, z);
        }

        @Override
        public Material getType() {
            Material material = Material.matchMaterial(world.tube.block(x, y, z).key());
            return material == null ? Material.AIR : material;
        }

        @Override
        public void setType(Material type) {
            world.tube.setBlock(x, y, z, space.nows.tube.api.Block.of(type.getKey().toString()));
        }

        @Override
        public void setType(Material type, boolean applyPhysics) {
            setType(type);
        }

        @Override
        public Block getRelative(BlockFace face) {
            return getRelative(face.getModX(), face.getModY(), face.getModZ());
        }

        @Override
        public Block getRelative(int dx, int dy, int dz) {
            return new BukkitBlock(runtime, world, x + dx, y + dy, z + dz);
        }

        @Override
        public boolean isEmpty() {
            return world.tube.block(x, y, z).isAir();
        }

        @Override
        public boolean isLiquid() {
            Material type = getType();
            return type == Material.WATER || type == Material.LAVA;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof BukkitBlock block && block.x == x && block.y == y && block.z == z && block.world.equals(world);
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z, world.tube.name());
        }
    }

    @SuppressWarnings("unused")
    private static Component unused() {
        return null;
    }
}

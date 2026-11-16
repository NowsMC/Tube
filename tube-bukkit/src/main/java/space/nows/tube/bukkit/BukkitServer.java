package space.nows.tube.bukkit;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Logger;

final class BukkitServer implements Server {

    private final BukkitRuntime runtime;

    BukkitServer(BukkitRuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public String getName() {
        return runtime.tube.name();
    }

    @Override
    public String getVersion() {
        return runtime.tube.name() + " " + runtime.tube.version() + " (MC: " + runtime.tube.minecraftVersion() + ")";
    }

    @Override
    public String getBukkitVersion() {
        return runtime.tube.minecraftVersion() + "-R0.1-SNAPSHOT";
    }

    @Override
    public Logger getLogger() {
        return runtime.julLogger;
    }

    @Override
    public PluginManager getPluginManager() {
        return runtime.pluginManager;
    }

    @Override
    public BukkitScheduler getScheduler() {
        return runtime.scheduler;
    }

    @Override
    public ServicesManager getServicesManager() {
        return runtime.services;
    }

    @Override
    public PluginCommand getPluginCommand(String name) {
        return runtime.commands.get(name);
    }

    @Override
    public ConsoleCommandSender getConsoleSender() {
        return runtime.wrappers.console();
    }

    @Override
    public Collection<? extends Player> getOnlinePlayers() {
        return runtime.wrappers.onlinePlayers();
    }

    @Override
    public int getMaxPlayers() {
        return runtime.tube.maxPlayers();
    }

    @Override
    public int getPort() {
        return runtime.tube.port();
    }

    @Override
    public String getIp() {
        return runtime.tube.bindAddress().equals("0.0.0.0") ? "" : runtime.tube.bindAddress();
    }

    @Override
    public String getMotd() {
        return runtime.tube.motd();
    }

    @Override
    public boolean getOnlineMode() {
        return runtime.tube.onlineMode();
    }

    @Override
    public Player getPlayer(String name) {
        Player exact = getPlayerExact(name);
        if (exact != null) {
            return exact;
        }
        String wanted = name.toLowerCase(Locale.ROOT);
        Player best = null;
        int delta = Integer.MAX_VALUE;
        for (Player player : runtime.wrappers.onlinePlayers()) {
            String lowered = player.getName().toLowerCase(Locale.ROOT);
            if (lowered.startsWith(wanted) && lowered.length() - wanted.length() < delta) {
                delta = lowered.length() - wanted.length();
                best = player;
            }
        }
        return best;
    }

    @Override
    public Player getPlayer(UUID id) {
        return runtime.wrappers.onlinePlayers().stream().filter(player -> player.getUniqueId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public Player getPlayerExact(String name) {
        return runtime.wrappers.onlinePlayers().stream().filter(player -> player.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Override
    public List<Player> matchPlayer(String name) {
        String wanted = name.toLowerCase(Locale.ROOT);
        List<Player> matches = new ArrayList<>();
        for (Player player : runtime.wrappers.onlinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) {
                return List.of(player);
            }
            if (player.getName().toLowerCase(Locale.ROOT).contains(wanted)) {
                matches.add(player);
            }
        }
        return matches;
    }

    @Override
    public OfflinePlayer getOfflinePlayer(String name) {
        Player online = getPlayerExact(name);
        return online != null ? online : runtime.wrappers.offline(Wrappers.offlineUuid(name), name);
    }

    @Override
    public OfflinePlayer getOfflinePlayer(UUID id) {
        Player online = getPlayer(id);
        return online != null ? online : runtime.wrappers.offline(id, null);
    }

    @Override
    public List<World> getWorlds() {
        return runtime.tube.worlds().all().stream().map(runtime.wrappers::world).toList();
    }

    @Override
    public World getWorld(String name) {
        return runtime.tube.worlds().get(name).map(runtime.wrappers::world).orElse(null);
    }

    @Override
    public World getWorld(UUID uid) {
        return getWorlds().stream().filter(world -> world.getUID().equals(uid)).findFirst().orElse(null);
    }

    @Override
    public File getWorldContainer() {
        return new File("worlds");
    }

    @Override
    public boolean dispatchCommand(CommandSender sender, String commandLine) {
        return runtime.commands.dispatch(sender, commandLine);
    }

    @Override
    public int broadcastMessage(String message) {
        runtime.tube.broadcast(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(message));
        return runtime.tube.players().size();
    }

    @Override
    public int broadcast(String message, String permission) {
        int delivered = 0;
        for (Player player : runtime.wrappers.onlinePlayers()) {
            if (player.hasPermission(permission)) {
                player.sendMessage(message);
                delivered++;
            }
        }
        return delivered;
    }

    @Override
    public boolean isPrimaryThread() {
        return runtime.isPrimaryThread();
    }

    @Override
    public boolean isStopping() {
        return false;
    }

    @Override
    public void shutdown() {
        runtime.tube.stop();
    }

    @Override
    public String getUpdateFolder() {
        return "update";
    }

    @Override
    public File getUpdateFolderFile() {
        return new File(runtime.pluginsDirectory, "update");
    }

    @Override
    public String toString() {
        return "TubeServer{name=" + runtime.tube.name() + ",version=" + runtime.tube.version() + "}";
    }
}

package org.bukkit;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public final class Bukkit {

    private static Server server;

    private Bukkit() {
    }

    public static Server getServer() {
        return server;
    }

    public static void setServer(Server server) {
        if (Bukkit.server != null) {
            throw new UnsupportedOperationException("Cannot redefine singleton Server");
        }
        Bukkit.server = server;
        server.getLogger().info("This server is running " + getName() + " version " + getVersion() + " (Implementing API version " + getBukkitVersion() + ")");
    }

    public static String getName() {
        return server.getName();
    }

    public static String getVersion() {
        return server.getVersion();
    }

    public static String getBukkitVersion() {
        return server.getBukkitVersion();
    }

    public static Logger getLogger() {
        return server.getLogger();
    }

    public static PluginManager getPluginManager() {
        return server.getPluginManager();
    }

    public static BukkitScheduler getScheduler() {
        return server.getScheduler();
    }

    public static ServicesManager getServicesManager() {
        return server.getServicesManager();
    }

    public static org.bukkit.boss.BossBar createBossBar(String title, org.bukkit.boss.BarColor color, org.bukkit.boss.BarStyle style, org.bukkit.boss.BarFlag... flags) {
        return server.createBossBar(title, color, style, flags);
    }

    public static org.bukkit.block.data.BlockData createBlockData(String data) {
        return server.createBlockData(data);
    }

    public static org.bukkit.plugin.messaging.Messenger getMessenger() {
        return server.getMessenger();
    }

    public static World createWorld(WorldCreator creator) {
        return server.createWorld(creator);
    }

    public static boolean unloadWorld(World world, boolean save) {
        return server.unloadWorld(world, save);
    }

    public static boolean unloadWorld(String name, boolean save) {
        return server.unloadWorld(name, save);
    }

    public static UnsafeValues getUnsafe() {
        return server.getUnsafe();
    }

    public static org.bukkit.inventory.ItemFactory getItemFactory() {
        return server.getItemFactory();
    }

    public static org.bukkit.help.HelpMap getHelpMap() {
        return server.getHelpMap();
    }

    public static PluginCommand getPluginCommand(String name) {
        return server.getPluginCommand(name);
    }

    public static ConsoleCommandSender getConsoleSender() {
        return server.getConsoleSender();
    }

    public static Collection<? extends Player> getOnlinePlayers() {
        return server.getOnlinePlayers();
    }

    public static int getMaxPlayers() {
        return server.getMaxPlayers();
    }

    public static int getPort() {
        return server.getPort();
    }

    public static String getIp() {
        return server.getIp();
    }

    public static String getMotd() {
        return server.getMotd();
    }

    public static boolean getOnlineMode() {
        return server.getOnlineMode();
    }

    public static Player getPlayer(String name) {
        return server.getPlayer(name);
    }

    public static Player getPlayer(UUID id) {
        return server.getPlayer(id);
    }

    public static Player getPlayerExact(String name) {
        return server.getPlayerExact(name);
    }

    public static List<Player> matchPlayer(String name) {
        return server.matchPlayer(name);
    }

    public static OfflinePlayer getOfflinePlayer(String name) {
        return server.getOfflinePlayer(name);
    }

    public static OfflinePlayer getOfflinePlayer(UUID id) {
        return server.getOfflinePlayer(id);
    }

    public static List<World> getWorlds() {
        return server.getWorlds();
    }

    public static World getWorld(String name) {
        return server.getWorld(name);
    }

    public static World getWorld(UUID uid) {
        return server.getWorld(uid);
    }

    public static File getWorldContainer() {
        return server.getWorldContainer();
    }

    public static boolean dispatchCommand(CommandSender sender, String commandLine) {
        return server.dispatchCommand(sender, commandLine);
    }

    public static int broadcastMessage(String message) {
        return server.broadcastMessage(message);
    }

    public static int broadcast(String message, String permission) {
        return server.broadcast(message, permission);
    }

    public static String getMinecraftVersion() {
        return server.getMinecraftVersion();
    }

    public static double[] getTPS() {
        return server.getTPS();
    }

    public static double getAverageTickTime() {
        return server.getAverageTickTime();
    }

    public static boolean isPrimaryThread() {
        return server.isPrimaryThread();
    }

    public static boolean isStopping() {
        return server.isStopping();
    }

    public static void shutdown() {
        server.shutdown();
    }
}

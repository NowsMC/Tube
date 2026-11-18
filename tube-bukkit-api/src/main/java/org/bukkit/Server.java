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

public interface Server {

    String getName();

    String getVersion();

    String getBukkitVersion();

    Logger getLogger();

    PluginManager getPluginManager();

    BukkitScheduler getScheduler();

    ServicesManager getServicesManager();

    org.bukkit.plugin.messaging.Messenger getMessenger();

    UnsafeValues getUnsafe();

    org.bukkit.inventory.ItemFactory getItemFactory();

    World createWorld(WorldCreator creator);

    boolean unloadWorld(World world, boolean save);

    boolean unloadWorld(String name, boolean save);

    org.bukkit.help.HelpMap getHelpMap();

    org.bukkit.packs.DataPackManager getDataPackManager();

    org.bukkit.boss.BossBar createBossBar(String title, org.bukkit.boss.BarColor color, org.bukkit.boss.BarStyle style, org.bukkit.boss.BarFlag... flags);

    org.bukkit.block.data.BlockData createBlockData(String data);

    PluginCommand getPluginCommand(String name);

    ConsoleCommandSender getConsoleSender();

    Collection<? extends Player> getOnlinePlayers();

    int getMaxPlayers();

    int getPort();

    String getIp();

    String getMotd();

    boolean getOnlineMode();

    Player getPlayer(String name);

    Player getPlayer(UUID id);

    Player getPlayerExact(String name);

    List<Player> matchPlayer(String name);

    OfflinePlayer getOfflinePlayer(String name);

    OfflinePlayer getOfflinePlayer(UUID id);

    List<World> getWorlds();

    World getWorld(String name);

    World getWorld(UUID uid);

    File getWorldContainer();

    boolean dispatchCommand(CommandSender sender, String commandLine);

    int broadcastMessage(String message);

    int broadcast(String message, String permission);

    boolean isPrimaryThread();

    String getMinecraftVersion();

    double[] getTPS();

    double getAverageTickTime();

    boolean isStopping();

    void shutdown();

    String getUpdateFolder();

    File getUpdateFolderFile();
}

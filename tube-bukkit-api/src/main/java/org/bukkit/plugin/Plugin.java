package org.bukkit.plugin;

import org.bukkit.Server;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;

public interface Plugin extends CommandExecutor {

    File getDataFolder();

    PluginDescriptionFile getDescription();

    FileConfiguration getConfig();

    InputStream getResource(String filename);

    void saveConfig();

    void saveDefaultConfig();

    void saveResource(String resourcePath, boolean replace);

    void reloadConfig();

    Server getServer();

    boolean isEnabled();

    void onDisable();

    void onLoad();

    void onEnable();

    boolean isNaggable();

    void setNaggable(boolean canNag);

    Logger getLogger();

    String getName();
}

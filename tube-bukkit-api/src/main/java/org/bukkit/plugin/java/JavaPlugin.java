package org.bukkit.plugin.java;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginBase;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class JavaPlugin extends PluginBase {

    private boolean enabled;
    private Server server;
    private File file;
    private PluginDescriptionFile description;
    private File dataFolder;
    private ClassLoader classLoader;
    private boolean naggable = true;
    private FileConfiguration newConfig;
    private File configFile;
    private Logger logger;

    public JavaPlugin() {
        if (!(getClass().getClassLoader() instanceof PluginClassLoader loader)) {
            throw new IllegalStateException("JavaPlugin requires " + PluginClassLoader.class.getName());
        }
        loader.initialize(this);
    }

    public static <T extends JavaPlugin> T getPlugin(Class<T> clazz) {
        if (!(clazz.getClassLoader() instanceof PluginClassLoader loader) || loader.getPlugin() == null) {
            throw new IllegalArgumentException(clazz + " does not belong to a loaded plugin");
        }
        return clazz.cast(loader.getPlugin());
    }

    public static JavaPlugin getProvidingPlugin(Class<?> clazz) {
        if (!(clazz.getClassLoader() instanceof PluginClassLoader loader) || loader.getPlugin() == null) {
            throw new IllegalArgumentException(clazz + " is not provided by a plugin");
        }
        return loader.getPlugin();
    }

    void init(Server server, PluginDescriptionFile description, File dataFolder, File file, ClassLoader classLoader) {
        this.server = server;
        this.file = file;
        this.description = description;
        this.dataFolder = dataFolder;
        this.classLoader = classLoader;
        this.configFile = new File(dataFolder, "config.yml");
        this.logger = new PluginLogger(this);
    }

    @Override
    public final File getDataFolder() {
        return dataFolder;
    }

    @Override
    public final PluginDescriptionFile getDescription() {
        return description;
    }

    @Override
    public FileConfiguration getConfig() {
        if (newConfig == null) {
            reloadConfig();
        }
        return newConfig;
    }

    @Override
    public void reloadConfig() {
        newConfig = YamlConfiguration.loadConfiguration(configFile);
        InputStream defaults = getResource("config.yml");
        if (defaults != null) {
            newConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defaults, StandardCharsets.UTF_8)));
        }
    }

    @Override
    public void saveConfig() {
        try {
            getConfig().save(configFile);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not save config to " + configFile, e);
        }
    }

    @Override
    public void saveDefaultConfig() {
        if (!configFile.exists()) {
            saveResource("config.yml", false);
        }
    }

    @Override
    public void saveResource(String resourcePath, boolean replace) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }
        String path = resourcePath.replace('\\', '/');
        InputStream in = getResource(path);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + path + "' cannot be found in " + file);
        }
        File target = new File(dataFolder, path);
        File parent = target.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            logger.log(Level.WARNING, "Could not create " + parent);
        }
        try (in) {
            if (!target.exists() || replace) {
                Files.copy(in, target.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } else {
                logger.log(Level.WARNING, "Could not save " + target.getName() + " to " + target + " because " + target.getName() + " already exists.");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not save " + target.getName() + " to " + target, e);
        }
    }

    @Override
    public InputStream getResource(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }
        try {
            URL url = ((java.net.URLClassLoader) classLoader).findResource(filename);
            if (url == null) {
                return null;
            }
            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    protected final ClassLoader getClassLoader() {
        return classLoader;
    }

    protected File getFile() {
        return file;
    }

    @Override
    public final Server getServer() {
        return server;
    }

    @Override
    public final boolean isEnabled() {
        return enabled;
    }

    public final void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) {
                onEnable();
            } else {
                onDisable();
            }
        }
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
    }

    @Override
    public final boolean isNaggable() {
        return naggable;
    }

    @Override
    public final void setNaggable(boolean canNag) {
        this.naggable = canNag;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public PluginCommand getCommand(String name) {
        PluginCommand command = server.getPluginCommand(name.toLowerCase(java.util.Locale.ROOT));
        if (command == null || command.getPlugin() != this) {
            command = server.getPluginCommand(description.getName().toLowerCase(java.util.Locale.ROOT) + ":" + name.toLowerCase(java.util.Locale.ROOT));
        }
        return command != null && command.getPlugin() == this ? command : null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    @Override
    public String toString() {
        return description.getFullName();
    }
}

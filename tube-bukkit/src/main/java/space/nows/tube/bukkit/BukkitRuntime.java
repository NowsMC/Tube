package space.nows.tube.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.java.PluginClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.event.Subscription;
import space.nows.tube.bukkit.command.CommandBridge;
import space.nows.tube.bukkit.event.EventBridge;
import space.nows.tube.bukkit.plugin.BukkitMessenger;
import space.nows.tube.bukkit.plugin.BukkitPluginManager;
import space.nows.tube.bukkit.plugin.BukkitServicesManager;
import space.nows.tube.bukkit.scheduler.BukkitTaskScheduler;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Level;

public final class BukkitRuntime {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube-bukkit");

    private final space.nows.tube.api.Server tube;
    private final File pluginsDirectory;
    private final java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger("Minecraft");
    private final BukkitTaskScheduler scheduler;
    private final BukkitServicesManager services = new BukkitServicesManager();
    private final BukkitMessenger messenger = new BukkitMessenger(this);
    private final Adapters adapters;
    private final CommandBridge commands;
    private final BukkitPluginManager pluginManager;
    private final Server server;

    private final List<Subscription> subscriptions = new ArrayList<>();
    private volatile Thread tickThread;

    private BukkitRuntime(space.nows.tube.api.Server tube, Path pluginsDirectory) {
        this.tube = tube;
        this.pluginsDirectory = pluginsDirectory.toFile();
        this.scheduler = new BukkitTaskScheduler(tube.scheduler());
        this.adapters = new Adapters(this);
        this.server = new BukkitServer(this);
        this.commands = new CommandBridge(this);
        this.pluginManager = new BukkitPluginManager(this);
    }

    static BukkitRuntime install(space.nows.tube.api.Server tube, Path pluginsDirectory) {
        Slf4jLogHandler.install();
        BukkitRuntime runtime = new BukkitRuntime(tube, pluginsDirectory);
        Bukkit.setServer(runtime.server());
        tube.scheduler().runSync(() -> runtime.tickThread = Thread.currentThread());
        return runtime;
    }

    static void forgetInstalledServer() {
        try {
            Field field = Bukkit.class.getDeclaredField("server");
            field.setAccessible(true);
            field.set(null, null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    static boolean hasBukkitDescriptor(File file) {
        try (JarFile jar = new JarFile(file)) {
            return jar.getEntry("plugin.yml") != null;
        } catch (java.io.IOException e) {
            return false;
        }
    }

    public space.nows.tube.api.Server tube() {
        return tube;
    }

    public File pluginsDirectory() {
        return pluginsDirectory;
    }

    public java.util.logging.Logger julLogger() {
        return julLogger;
    }

    public BukkitTaskScheduler scheduler() {
        return scheduler;
    }

    public BukkitServicesManager services() {
        return services;
    }

    public BukkitMessenger messenger() {
        return messenger;
    }

    public Adapters adapters() {
        return adapters;
    }

    public CommandBridge commands() {
        return commands;
    }

    public BukkitPluginManager pluginManager() {
        return pluginManager;
    }

    public Server server() {
        return server;
    }

    public boolean isPrimaryThread() {
        Thread current = Thread.currentThread();
        return current == tickThread || current.getName().startsWith("Ms-Tick");
    }

    void useCurrentThreadAsTickThread() {
        tickThread = Thread.currentThread();
    }

    void loadPlugins() {
        if (!pluginsDirectory.isDirectory() && !pluginsDirectory.mkdirs()) {
            LOGGER.warn("Could not create {}", pluginsDirectory);
        }
        File[] jars = pluginsDirectory.listFiles(file -> file.getName().endsWith(".jar") && hasBukkitDescriptor(file));
        Plugin[] loaded = pluginManager.loadPlugins(jars == null ? new File[0] : jars);
        for (Plugin plugin : loaded) {
            try {
                plugin.getLogger().info("Loading " + plugin.getDescription().getFullName());
                plugin.onLoad();
            } catch (Throwable e) {
                julLogger.log(Level.SEVERE, e.getMessage() + " initializing " + plugin.getDescription().getFullName() + " (Is it up to date?)", e);
                pluginManager.disablePlugin(plugin);
            }
        }
        enablePlugins(PluginLoadOrder.STARTUP);
        enablePlugins(PluginLoadOrder.POSTWORLD);
        new EventBridge(this).install(tube.events(), subscriptions);
    }

    private void enablePlugins(PluginLoadOrder order) {
        for (Plugin plugin : pluginManager.getPlugins()) {
            if (!plugin.isEnabled() && plugin.getDescription().getLoad() == order) {
                for (Permission permission : plugin.getDescription().getPermissions()) {
                    try {
                        pluginManager.addPermission(permission);
                    } catch (IllegalArgumentException e) {
                        julLogger.log(Level.WARNING, "Plugin " + plugin.getDescription().getFullName() + " tried to register permission '" + permission.getName() + "' but it's already registered");
                    }
                }
                pluginManager.enablePlugin(plugin);
            }
        }
    }

    void shutdown() {
        subscriptions.forEach(Subscription::cancel);
        subscriptions.clear();
        pluginManager.disablePlugins();
        scheduler.shutdown();
        commands.unregisterAll();
        PluginClassLoader.closeAll();
        forgetInstalledServer();
    }
}

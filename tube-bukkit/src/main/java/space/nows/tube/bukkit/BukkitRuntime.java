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

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Level;

final class BukkitRuntime {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube-bukkit");

    final space.nows.tube.api.Server tube;
    final File pluginsDirectory;
    final java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger("Minecraft");
    final TubeBukkitScheduler scheduler;
    final BukkitServices services = new BukkitServices();
    final Wrappers wrappers;
    final BukkitCommands commands;
    final BukkitPluginManager pluginManager;
    final Server server;

    private final List<Subscription> subscriptions = new ArrayList<>();
    private volatile Thread tickThread;

    private BukkitRuntime(space.nows.tube.api.Server tube, Path pluginsDirectory) {
        this.tube = tube;
        this.pluginsDirectory = pluginsDirectory.toFile();
        this.scheduler = new TubeBukkitScheduler(tube.scheduler());
        this.wrappers = new Wrappers(this);
        this.commands = new BukkitCommands(this);
        this.pluginManager = new BukkitPluginManager(this);
        this.server = new BukkitServer(this);
    }

    static BukkitRuntime install(space.nows.tube.api.Server tube, Path pluginsDirectory) {
        TubeLogHandler.install();
        BukkitRuntime runtime = new BukkitRuntime(tube, pluginsDirectory);
        Bukkit.setServer(runtime.server);
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

    boolean isPrimaryThread() {
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
        new BukkitEventBridge(this).install(tube.events(), subscriptions);
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

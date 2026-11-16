package space.nows.tube.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.help.HelpMap;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.SimpleServicesManager;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.event.Subscription;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarFile;
import java.util.logging.Level;

final class BukkitRuntime {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube-bukkit");

    final space.nows.tube.api.Server tube;
    final File pluginsDirectory;
    final java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger("Minecraft");
    final TubeBukkitScheduler scheduler;
    final SimpleServicesManager services = new SimpleServicesManager();
    final StandardMessenger messenger = new StandardMessenger();
    final HelpMap helpMap = Proxies.of(HelpMap.class, "HelpMap")
            .on("getHelpTopic", args -> null)
            .on("getHelpTopics", args -> List.of())
            .on("getIgnoredPlugins", args -> List.of())
            .on("addTopic", args -> null)
            .on("clear", args -> null)
            .on("registerHelpTopicFactory", args -> null)
            .build();
    final org.bukkit.UnsafeValues unsafe = Proxies.of(org.bukkit.UnsafeValues.class, "UnsafeValues")
            .on("checkSupported", args -> null)
            .on("processClass", args -> args[2])
            .build();
    final Wrappers wrappers;
    final Server server;
    final SimpleCommandMap commandMap;
    final SimplePluginManager pluginManager;

    private final List<String> bridgedCommands = new CopyOnWriteArrayList<>();
    private final List<Subscription> subscriptions = new ArrayList<>();
    private volatile Thread tickThread;
    private volatile Thread lifecycleThread;

    private BukkitRuntime(space.nows.tube.api.Server tube, Path pluginsDirectory) {
        this.tube = tube;
        this.pluginsDirectory = pluginsDirectory.toFile();
        this.scheduler = new TubeBukkitScheduler(tube.scheduler());
        this.wrappers = new Wrappers(this);
        this.server = TubeBukkitServer.create(this);
        this.commandMap = new SimpleCommandMap(server);
        this.pluginManager = new SimplePluginManager(server, commandMap);
    }

    static BukkitRuntime install(space.nows.tube.api.Server tube, Path pluginsDirectory) {
        TubeLogHandler.install();
        BukkitRuntime runtime = new BukkitRuntime(tube, pluginsDirectory);
        Bukkit.setServer(runtime.server);
        runtime.pluginManager.registerInterface(JavaPluginLoader.class);
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
        return current == tickThread || current == lifecycleThread || current.getName().startsWith("Ms-Tick");
    }

    void useCurrentThreadAsTickThread() {
        tickThread = Thread.currentThread();
    }

    void loadPlugins() {
        lifecycleThread = Thread.currentThread();
        try {
            loadPluginsOnLifecycleThread();
        } finally {
            lifecycleThread = null;
        }
    }

    private void loadPluginsOnLifecycleThread() {
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

    void enablePlugins(PluginLoadOrder order) {
        for (Plugin plugin : pluginManager.getPlugins()) {
            if (!plugin.isEnabled() && plugin.getDescription().getLoad() == order) {
                enable(plugin);
            }
        }
    }

    void enable(Plugin plugin) {
        for (Permission permission : plugin.getDescription().getPermissions()) {
            try {
                pluginManager.addPermission(permission, false);
            } catch (IllegalArgumentException e) {
                julLogger.log(Level.WARNING, "Plugin " + plugin.getDescription().getFullName() + " tried to register permission '" + permission.getName() + "' but it's already registered", e);
            }
        }
        pluginManager.dirtyPermissibles();
        pluginManager.enablePlugin(plugin);
        if (plugin.isEnabled()) {
            bridgeCommands(plugin);
        }
    }

    void shutdown() {
        lifecycleThread = Thread.currentThread();
        subscriptions.forEach(Subscription::cancel);
        subscriptions.clear();
        pluginManager.disablePlugins();
        lifecycleThread = null;
        scheduler.shutdown();
        bridgedCommands.forEach(tube.commands()::unregister);
        bridgedCommands.clear();
        forgetInstalledServer();
    }

    ConsoleCommandSender console() {
        return wrappers.console();
    }

    private void bridgeCommands(Plugin plugin) {
        for (Command registered : new java.util.LinkedHashSet<>(commandMap.getCommands())) {
            if (!(registered instanceof PluginCommand command) || command.getPlugin() != plugin) {
                continue;
            }
            List<String> labels = new ArrayList<>();
            labels.add(command.getName());
            labels.addAll(command.getAliases());
            for (String label : labels) {
                try {
                    tube.commands().register(label, (sender, args) -> {
                        CommandSender bukkitSender = wrappers.sender(sender);
                        String line = args.isEmpty() ? label : label + " " + String.join(" ", args);
                        if (!commandMap.dispatch(bukkitSender, line)) {
                            bukkitSender.sendMessage("Unknown command: " + label);
                        }
                    });
                    bridgedCommands.add(label);
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Command /{} of {} clashes with a Tube command and was not exposed", label, plugin.getName());
                }
            }
        }
    }
}

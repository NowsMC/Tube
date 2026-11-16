package space.nows.tube.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.SimpleServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

final class BukkitRuntime {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube-bukkit");

    final space.nows.tube.api.Server tube;
    final File pluginsDirectory;
    final java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger("Minecraft");
    final TubeBukkitScheduler scheduler;
    final SimpleServicesManager services = new SimpleServicesManager();
    final org.bukkit.plugin.messaging.StandardMessenger messenger = new org.bukkit.plugin.messaging.StandardMessenger();
    final org.bukkit.help.HelpMap helpMap = Proxies.of(org.bukkit.help.HelpMap.class, "HelpMap")
            .on("getHelpTopic", args -> null)
            .on("getHelpTopics", args -> List.of())
            .on("getIgnoredPlugins", args -> List.of())
            .on("addTopic", args -> null)
            .on("clear", args -> null)
            .on("registerHelpTopicFactory", args -> null)
            .build();
    final Wrappers wrappers;
    final Server server;
    final SimpleCommandMap commandMap;
    final org.bukkit.plugin.SimplePluginManager pluginManager;
    final TubePluginManager lifecycle;

    private final List<String> bridgedCommands = new CopyOnWriteArrayList<>();
    private final List<space.nows.tube.api.event.Subscription> subscriptions = new ArrayList<>();
    private volatile Thread tickThread;

    private BukkitRuntime(space.nows.tube.api.Server tube, Path pluginsDirectory) {
        this.tube = tube;
        this.pluginsDirectory = pluginsDirectory.toFile();
        this.scheduler = new TubeBukkitScheduler(tube.scheduler());
        this.wrappers = new Wrappers(this);
        this.server = TubeBukkitServer.create(this);
        this.commandMap = new SimpleCommandMap(server, new HashMap<>());
        this.lifecycle = new TubePluginManager(server, commandMap);
        this.pluginManager = new org.bukkit.plugin.SimplePluginManager(server, commandMap);
        this.pluginManager.paperPluginManager = lifecycle;
    }

    static BukkitRuntime install(space.nows.tube.api.Server tube, Path pluginsDirectory) {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        TubeBuildInfo.configure(tube.version(), tube.minecraftVersion());
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

    boolean isPrimaryThread() {
        Thread current = Thread.currentThread();
        return current == tickThread || current.getName().startsWith("Ms-Tick");
    }

    void loadPlugins() {
        if (!pluginsDirectory.isDirectory() && !pluginsDirectory.mkdirs()) {
            LOGGER.warn("Could not create {}", pluginsDirectory);
        }
        Plugin[] loaded = pluginManager.loadPlugins(pluginsDirectory);
        for (Plugin plugin : loaded) {
            try {
                plugin.getLogger().info("Loading " + plugin.getPluginMeta().getDisplayName());
                plugin.onLoad();
            } catch (Throwable e) {
                julLogger.log(Level.SEVERE, e.getMessage() + " initializing " + plugin.getPluginMeta().getDisplayName() + " (Is it up to date?)", e);
                pluginManager.disablePlugin(plugin);
            }
        }
        enablePlugins(PluginLoadOrder.STARTUP);
        enablePlugins(PluginLoadOrder.POSTWORLD);
        new BukkitEventBridge(this).install(tube.events(), subscriptions);
    }

    void enablePlugins(PluginLoadOrder order) {
        for (Plugin plugin : pluginManager.getPlugins()) {
            if (!plugin.isEnabled() && plugin.getPluginMeta() != null && ((org.bukkit.plugin.PluginDescriptionFile) plugin.getPluginMeta()).getLoad() == order) {
                enable(plugin);
            }
        }
    }

    void enable(Plugin plugin) {
        for (Permission permission : plugin.getDescription().getPermissions()) {
            try {
                pluginManager.addPermission(permission);
            } catch (IllegalArgumentException e) {
                LOGGER.debug("Permission {} was already registered", permission.getName());
            }
        }
        pluginManager.enablePlugin(plugin);
        if (plugin.isEnabled()) {
            bridgeCommands(plugin);
        }
    }

    void shutdown() {
        subscriptions.forEach(space.nows.tube.api.event.Subscription::cancel);
        subscriptions.clear();
        pluginManager.disablePlugins();
        scheduler.shutdown();
        TubePluginClassLoader.closeAll();
        bridgedCommands.forEach(tube.commands()::unregister);
        bridgedCommands.clear();
        forgetInstalledServer();
    }

    ConsoleCommandSender console() {
        return wrappers.console();
    }

    private void bridgeCommands(Plugin plugin) {
        for (Map.Entry<String, Command> entry : new ArrayList<>(commandMap.getKnownCommands().entrySet())) {
            String label = entry.getKey();
            if (label.indexOf(':') >= 0 || !(entry.getValue() instanceof PluginCommand command) || command.getPlugin() != plugin) {
                continue;
            }
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

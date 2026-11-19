package space.nows.tube.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.world.Difficulty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.Server;
import space.nows.tube.api.ServerExtension;
import space.nows.tube.api.TickReport;
import space.nows.tube.api.command.CommandRegistry;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.argument.TargetResolver;
import space.nows.tube.api.entity.player.GameMode;
import space.nows.tube.api.entity.player.Player;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.inventory.Inventory;
import space.nows.tube.api.network.Network;
import space.nows.tube.api.permission.PermissionManager;
import space.nows.tube.api.plugin.PluginDescriptor;
import space.nows.tube.api.registry.Registries;
import space.nows.tube.api.scheduler.Scheduler;
import space.nows.tube.api.world.WorldManager;
import space.nows.tube.api.world.WorldOptions;
import space.nows.tube.network.NettyNetwork;
import space.nows.tube.network.Transport;
import space.nows.tube.server.access.AccessControl;
import space.nows.tube.server.access.BanList;
import space.nows.tube.server.access.PermissionService;
import space.nows.tube.server.access.Profiles;
import space.nows.tube.server.access.UserCache;
import space.nows.tube.server.access.Whitelist;
import space.nows.tube.server.command.CommandService;
import space.nows.tube.server.command.builtin.BuiltinCommands;
import space.nows.tube.server.command.builtin.server.ServerControls;
import space.nows.tube.server.command.support.EngineRegistries;
import space.nows.tube.server.command.support.Targets;
import space.nows.tube.server.config.ServerProperties;
import space.nows.tube.server.config.ServerSettings;
import space.nows.tube.server.connection.ConnectionLog;
import space.nows.tube.server.connection.LoginGate;
import space.nows.tube.server.connection.LoginHandler;
import space.nows.tube.server.connection.PacketRateLimiter;
import space.nows.tube.server.connection.PluginChannelService;
import space.nows.tube.server.connection.ResourcePackHandler;
import space.nows.tube.server.connection.StatusResponder;
import space.nows.tube.server.console.TubeConsole;
import space.nows.tube.server.entity.ItemDrops;
import space.nows.tube.server.event.DefaultEventBus;
import space.nows.tube.server.event.EventBridge;
import space.nows.tube.server.gameplay.SurvivalRules;
import space.nows.tube.server.inventory.MinestomInventory;
import space.nows.tube.server.player.CommandAccess;
import space.nows.tube.server.player.IdleKicker;
import space.nows.tube.server.player.PlayerDataStore;
import space.nows.tube.server.player.PlayerService;
import space.nows.tube.server.plugin.PluginManager;
import space.nows.tube.server.remote.RemoteAccess;
import space.nows.tube.server.scheduler.SchedulerService;
import space.nows.tube.server.world.SpawnProtection;
import space.nows.tube.server.world.WorldService;
import space.nows.tube.server.world.vanilla.WorldgenLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DedicatedServer implements Server {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");
    private static final String RESTART_PROPERTY = "tube.restart.exit";

    private final VersionProfile profile;
    private final Path directory;
    private final TubeConsole console;
    private final String version = readVersion();
    private final AtomicBoolean stopped = new AtomicBoolean();
    private final List<ServerExtension> extensions = new ArrayList<>();

    private ServerSettings settings;
    private PermissionService permissions;
    private WorldService worlds;
    private PlayerService players;
    private space.nows.tube.server.scoreboard.ScoreboardService scoreboards;
    private PlayerDataStore playerData;
    private CommandService commands;
    private SchedulerService scheduler;
    private DefaultEventBus events;
    private PluginManager plugins;
    private NettyNetwork network;
    private RemoteAccess remoteAccess;
    private final Targets targets = new Targets(this);
    private final Registries registries = new EngineRegistries();
    private Autosave autosave;
    private final AutosaveGate autosaveGate = new AutosaveGate();
    private TickWatchdog watchdog;
    private Path propertiesFile;
    private volatile GameMode defaultGameMode;
    private final TickTracker ticks = new TickTracker(System::nanoTime);

    public DedicatedServer(VersionProfile profile, Path directory) {
        this(profile, directory, null);
    }

    public DedicatedServer(VersionProfile profile, Path directory, TubeConsole console) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.directory = Objects.requireNonNull(directory, "directory");
        this.console = console;
    }

    @Override
    public String name() {
        return "Tube";
    }

    @Override
    public String version() {
        return version;
    }

    @Override
    public String minecraftVersion() {
        return profile.minecraftVersion();
    }

    @Override
    public int dataVersion() {
        return MinecraftServer.DATA_VERSION;
    }

    @Override
    public int maxPlayers() {
        return settings.connection().maxPlayers();
    }

    @Override
    public boolean onlineMode() {
        return settings.connection().onlineMode();
    }

    @Override
    public String motd() {
        return settings.status().motd();
    }

    @Override
    public int port() {
        return settings.connection().port();
    }

    @Override
    public String bindAddress() {
        return settings.connection().bindAddress();
    }

    @Override
    public CommandRegistry commands() {
        return commands;
    }

    @Override
    public PermissionManager permissions() {
        return permissions;
    }

    @Override
    public WorldManager worlds() {
        return worlds;
    }

    @Override
    public Network network() {
        return network;
    }

    @Override
    public space.nows.tube.api.scoreboard.Scoreboards scoreboards() {
        return scoreboards;
    }

    @Override
    public Scheduler scheduler() {
        return scheduler;
    }

    @Override
    public EventBus events() {
        return events;
    }

    @Override
    public Collection<Player> players() {
        return players.online();
    }

    @Override
    public int protocolVersion() {
        return MinecraftServer.PROTOCOL_VERSION;
    }

    @Override
    public CommandSender console() {
        return commands.console();
    }

    @Override
    public TargetResolver targets() {
        return targets;
    }

    @Override
    public Registries registries() {
        return registries;
    }

    @Override
    public Collection<PluginDescriptor> plugins() {
        return plugins.descriptors();
    }

    @Override
    public space.nows.tube.api.world.Difficulty difficulty() {
        return space.nows.tube.api.world.Difficulty.valueOf(MinecraftServer.getDifficulty().name());
    }

    @Override
    public void setDifficulty(space.nows.tube.api.world.Difficulty difficulty) {
        MinecraftServer.setDifficulty(Difficulty.valueOf(difficulty.name()));
        ServerProperties.set(propertiesFile, "difficulty", difficulty.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public GameMode defaultGameMode() {
        return defaultGameMode;
    }

    @Override
    public void setDefaultGameMode(GameMode gameMode) {
        defaultGameMode = gameMode;
        ServerProperties.set(propertiesFile, "gamemode", gameMode.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public TickReport ticks() {
        return ticks.report();
    }

    @Override
    public Inventory createInventory(Component title, int rows) {
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("rows must be between 1 and 6");
        }
        return new MinestomInventory(new net.minestom.server.inventory.Inventory(InventoryType.valueOf("CHEST_" + rows + "_ROW"), title));
    }

    @Override
    public void broadcast(Component message) {
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> player.sendMessage(message));
        LOGGER.info(PlainTextComponentSerializer.plainText().serialize(message));
    }

    @Override
    public void saveAll() {
        worlds.saveAll();
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(this::savePlayer);
    }

    @Override
    public void stop() {
        Thread thread = new Thread(() -> {
            shutdown();
            Runtime.getRuntime().exit(0);
        }, "tube-stop");
        thread.start();
    }

    private final Restarter restarter = new Restarter() {
        @Override
        public boolean available() {
            return Integer.getInteger(RESTART_PROPERTY) != null;
        }

        @Override
        public void restart() {
            int code = Integer.getInteger(RESTART_PROPERTY);
            new Thread(() -> {
                shutdown();
                Runtime.getRuntime().exit(code);
            }, "tube-restart").start();
        }
    };

    public void start() {
        if (!Eula.isAccepted(directory)) {
            LOGGER.error("You need to agree to the EULA in order to run the server. Go to {} for more info.", Eula.FILE_NAME);
            Runtime.getRuntime().exit(1);
            return;
        }
        propertiesFile = directory.resolve("server.properties");
        settings = ServerProperties.load(propertiesFile);
        defaultGameMode = settings.gameplay().gamemode();
        LOGGER.info("Starting {} {} for Minecraft {}", name(), version, minecraftVersion());
        LOGGER.info("Running Java {} ({} {}; {}) on {} {} ({})", Runtime.version().feature(),
                System.getProperty("java.vm.name"), System.getProperty("java.vm.version"), System.getProperty("java.vm.vendor"),
                System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));

        EngineProperties.apply(settings);

        MinecraftServer server = MinecraftServer.init(settings.connection().onlineMode() ? new Auth.Online() : new Auth.Offline());
        MinecraftServer.setBrandName(name());
        int threshold = settings.network().compressionThreshold();
        MinecraftServer.setCompressionThreshold(threshold < 0 ? 0 : Math.max(1, threshold));
        MinecraftServer.setDifficulty(Difficulty.valueOf(settings.gameplay().difficulty().name()));

        permissions = new PermissionService(directory);
        UserCache userCache = new UserCache(directory);
        Whitelist whitelist = new Whitelist(directory, settings.access().whitelist());
        BanList bans = new BanList(directory);
        worlds = new WorldService(directory.resolve("worlds"), settings.level().name(), defaultWorldOptions(settings.level()),
                new WorldgenLoader(directory.resolve("cache"), minecraftVersion()));
        playerData = new PlayerDataStore(directory.resolve("worlds").resolve(settings.level().name()).resolve("playerdata"));
        players = new PlayerService(worlds, permissions);
        scoreboards = new space.nows.tube.server.scoreboard.ScoreboardService(players);
        commands = new CommandService(players);
        scheduler = new SchedulerService();
        events = new DefaultEventBus();
        players.attachEvents(events);
        commands.attachEvents(events);
        PluginChannelService channels = new PluginChannelService(players);
        network = new NettyNetwork(channels, Transport.choose(settings.network().nativeTransport()));
        LOGGER.info("Using {} for Tube network sockets", network.transport());
        plugins = new PluginManager(this, commands, scheduler, events, directory.resolve("plugins"));

        var handler = MinecraftServer.getGlobalEventHandler();
        ResourcePackHandler resourcePacks = new ResourcePackHandler(settings.resourcePack(), events, players);
        new LoginGate(settings.connection().onlineMode(), events, players).install(handler);
        new LoginHandler(settings, this::defaultGameMode, new AccessControl(whitelist, bans), userCache, players, playerData, worlds, resourcePacks.pack(), this::savePlayer).install(handler);
        ticks.install(handler);
        scoreboards.install(handler);
        resourcePacks.install(handler);
        new StatusResponder(settings, events, name() + " " + minecraftVersion()).install(handler);
        EventBridge bridge = new EventBridge(events, players, worlds);
        bridge.install(handler);
        new ItemDrops(events, players, worlds).install(handler);
        SurvivalRules survival = new SurvivalRules(directory.resolve("cache"), minecraftVersion(), worlds, events, Math.min(6, settings.level().viewDistance()));
        survival.install(handler);
        bridge.setDeathDrops(survival.deathDrops());
        new SpawnProtection(settings.level().spawnProtection(), worlds, permissions).install(events);
        new CommandAccess(permissions).install(handler);
        ConnectionLog.install(events, settings.connection().logIps());
        new PacketRateLimiter(settings.network().rateLimit(), System::nanoTime).install(handler);
        IdleKicker idleKicker = new IdleKicker(settings.gameplay().playerIdleTimeout(), System::currentTimeMillis);
        idleKicker.install(this);
        channels.install(handler);

        Profiles profiles = new Profiles(this::players, userCache);
        new BuiltinCommands(this, commands, profiles, bans, whitelist, propertiesFile, settings.access().enforceWhitelist(),
                new ServerControls(autosaveGate, idleKicker, whitelist, bans, plugins::reloadAll, restarter)).registerAll();
        LOGGER.info("Default game mode: {}, difficulty: {}", defaultGameMode.name().toLowerCase(Locale.ROOT),
                settings.gameplay().difficulty().name().toLowerCase(Locale.ROOT));
        long worldStart = System.nanoTime();
        LOGGER.info("Preparing world \"{}\"", settings.level().name());
        worlds.defaultWorld();
        LOGGER.info("Done preparing world \"{}\" ({})", settings.level().name(), seconds(System.nanoTime() - worldStart));
        plugins.loadAll();
        enableExtensions();
        remoteAccess = new RemoteAccess(this, settings, commands);
        remoteAccess.start(network);

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "tube-shutdown"));
        autosave = Autosave.start(settings.level().autosaveSeconds(), autosaveGate, worlds::saveAll,
                () -> scheduler.runSync(() -> MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(this::savePlayer)));
        if (console != null) {
            console.start(scheduler, commands);
        }
        String serverIp = settings.connection().serverIp();
        LOGGER.info("Starting Minecraft server on {}:{}", serverIp.isBlank() ? "*" : serverIp, settings.connection().port());
        server.start(settings.connection().bindAddress(), settings.connection().port());
        watchdog = TickWatchdog.start(settings.watchdog().maxTickTime(), scheduler);
        LOGGER.info("Done ({})! For help, type \"help\"", seconds(ManagementFactory.getRuntimeMXBean().getUptime() * 1_000_000L));
    }

    private static String seconds(long nanos) {
        return String.format(Locale.ROOT, "%.3fs", nanos / 1e9);
    }

    static WorldOptions defaultWorldOptions(ServerSettings.Level level) {
        String generator = switch (level.type().toLowerCase(java.util.Locale.ROOT)) {
            case "minecraft:normal", "normal", "minecraft:default", "default" -> WorldOptions.NORMAL;
            case "minecraft:flat", "flat" -> WorldOptions.FLAT;
            case "minecraft:void", "void" -> WorldOptions.VOID;
            default -> {
                LOGGER.warn("server.properties: level-type {} is not supported, using minecraft:normal", level.type());
                yield WorldOptions.NORMAL;
            }
        };
        String seed = level.seed();
        long value;
        if (seed.isBlank()) {
            value = new java.util.Random().nextLong();
        } else {
            try {
                value = Long.parseLong(seed);
            } catch (NumberFormatException e) {
                value = seed.hashCode();
            }
        }
        return WorldOptions.of(generator, value);
    }

    private void savePlayer(net.minestom.server.entity.Player player) {
        if (player.getInstance() == null) {
            return;
        }
        try {
            SurvivalRules.prepareForSave(player);
            playerData.save(player, worlds.of(player.getInstance()).name());
        } catch (RuntimeException e) {
            LOGGER.error("Could not save the data of {}", player.getUsername(), e);
        }
    }

    private void enableExtensions() {
        for (ServerExtension extension : ServiceLoader.load(ServerExtension.class, ServerExtension.class.getClassLoader())) {
            try {
                extension.enable(this, directory.resolve("plugins"));
                extensions.add(extension);
                LOGGER.info("Enabled extension {}", extension.name());
            } catch (Exception | LinkageError e) {
                LOGGER.error("Extension {} failed to enable", extension.name(), e);
            }
        }
    }

    private void shutdown() {
        if (!stopped.compareAndSet(false, true)) {
            return;
        }
        LOGGER.info("Stopping server");
        if (watchdog != null) {
            watchdog.close();
        }
        if (autosave != null) {
            autosave.close();
        }
        extensions.reversed().forEach(extension -> {
            try {
                extension.disable();
            } catch (RuntimeException e) {
                LOGGER.error("Extension {} failed to disable", extension.name(), e);
            }
        });
        plugins.disableAll();
        if (remoteAccess != null) {
            remoteAccess.close();
        }
        if (network != null) {
            network.close();
        }
        try {
            saveAll();
        } catch (RuntimeException e) {
            LOGGER.error("Could not save worlds", e);
        }
        MinecraftServer.stopCleanly();
    }

    private static String readVersion() {
        try (InputStream in = DedicatedServer.class.getResourceAsStream("/tube-server.properties")) {
            if (in == null) {
                throw new IllegalStateException("tube-server.properties is missing from the classpath");
            }
            Properties properties = new Properties();
            properties.load(in);
            return properties.getProperty("version");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

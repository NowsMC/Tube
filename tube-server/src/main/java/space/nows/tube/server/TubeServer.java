package space.nows.tube.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerResourcePackStatusEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.ping.Status;
import net.minestom.server.world.Difficulty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.CommandRegistry;
import space.nows.tube.api.Inventory;
import space.nows.tube.api.Location;
import space.nows.tube.api.PermissionManager;
import space.nows.tube.api.Player;
import space.nows.tube.api.PluginDescriptor;
import space.nows.tube.api.Scheduler;
import space.nows.tube.api.ServerExtension;
import space.nows.tube.api.Server;
import space.nows.tube.api.WorldManager;
import space.nows.tube.api.event.BlockBreakEvent;
import space.nows.tube.api.event.BlockPlaceEvent;
import space.nows.tube.api.event.EventPriority;
import space.nows.tube.api.event.PlayerJoinEvent;
import space.nows.tube.api.event.PlayerQuitEvent;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.event.PlayerResourcePackEvent;
import space.nows.tube.api.network.Network;
import space.nows.tube.network.NettyNetwork;
import space.nows.tube.network.QueryServer;
import space.nows.tube.network.RconServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TubeServer implements Server {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");

    private final VersionProfile profile;
    private final Path directory;
    private final String version = readVersion();
    private final AtomicBoolean stopped = new AtomicBoolean();

    private ServerSettings settings;
    private PermissionService permissions;
    private UserCache userCache;
    private Whitelist whitelist;
    private BanList bans;
    private PlayerDataStore playerData;
    private AccessControl access;
    private WorldService worlds;
    private PlayerService players;
    private CommandService commands;
    private SchedulerService scheduler;
    private DefaultEventBus events;
    private PluginManager plugins;
    private NettyNetwork network;
    private TubePluginChannels channels;
    private RconServer rcon;
    private QueryServer query;
    private ScheduledExecutorService autosave;
    private final java.util.List<ServerExtension> extensions = new java.util.ArrayList<>();

    public TubeServer(VersionProfile profile, Path directory) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.directory = Objects.requireNonNull(directory, "directory");
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
    public int maxPlayers() {
        return settings.maxPlayers();
    }

    @Override
    public boolean onlineMode() {
        return settings.onlineMode();
    }

    @Override
    public String motd() {
        return settings.motd();
    }

    @Override
    public int port() {
        return settings.serverPort();
    }

    @Override
    public String bindAddress() {
        return settings.bindAddress();
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
    public Collection<PluginDescriptor> plugins() {
        return plugins.descriptors();
    }

    @Override
    public Inventory createInventory(Component title, int rows) {
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("rows must be between 1 and 6");
        }
        return new TubeInventory(new net.minestom.server.inventory.Inventory(InventoryType.valueOf("CHEST_" + rows + "_ROW"), title));
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

    public void start() {
        if (!Eula.isAccepted(directory)) {
            LOGGER.error("You need to agree to the EULA in order to run the server. Go to {} for more info.", Eula.FILE_NAME);
            Runtime.getRuntime().exit(1);
            return;
        }
        settings = ServerSettings.load(directory.resolve("server.properties"));
        LOGGER.info("Starting {} {} for Minecraft {}", name(), version, minecraftVersion());

        System.setProperty("minestom.chunk-view-distance", Integer.toString(settings.viewDistance()));
        if (!settings.levelType().equals("minecraft:flat")) {
            LOGGER.warn("server.properties: level-type {} is not supported yet, using minecraft:flat", settings.levelType());
        }

        MinecraftServer server = MinecraftServer.init(settings.onlineMode() ? new Auth.Online() : new Auth.Offline());
        MinecraftServer.setBrandName(name());
        int threshold = settings.networkCompressionThreshold();
        MinecraftServer.setCompressionThreshold(threshold < 0 ? 0 : Math.max(1, threshold));
        MinecraftServer.setDifficulty(Difficulty.valueOf(settings.difficulty().name()));

        permissions = new PermissionService(directory);
        userCache = new UserCache(directory);
        whitelist = new Whitelist(directory, settings.whiteList());
        bans = new BanList(directory);
        access = new AccessControl(whitelist, bans);
        worlds = new WorldService(directory.resolve("worlds"), settings.levelName());
        playerData = new PlayerDataStore(directory.resolve("worlds").resolve(settings.levelName()).resolve("playerdata"));
        players = new PlayerService(worlds, permissions);
        commands = new CommandService(players);
        scheduler = new SchedulerService();
        events = new DefaultEventBus();
        players.attachEvents(events);
        channels = new TubePluginChannels(players);
        network = new NettyNetwork(channels);
        plugins = new PluginManager(this, commands, scheduler, events, directory.resolve("plugins"));

        installConnectionHandlers();
        new EventBridge(events, players, worlds).install(MinecraftServer.getGlobalEventHandler());
        installServerListeners();
        channels.install(MinecraftServer.getGlobalEventHandler());

        Profiles profiles = new Profiles(this::players, userCache);
        BuiltinCommands.register(this, commands, profiles);
        new VanillaCommands(this).register(commands);
        new AccessCommands(this, profiles, bans, whitelist, directory.resolve("server.properties"), settings.enforceWhitelist()).register(commands);
        worlds.defaultWorld();
        plugins.loadAll();
        enableExtensions();
        startRemoteAccess();

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "tube-shutdown"));
        startAutosave();
        startConsole();
        server.start(settings.bindAddress(), settings.serverPort());
    }

    private void installConnectionHandlers() {
        var handler = MinecraftServer.getGlobalEventHandler();
        Component motd = parseMotd(settings.motd());
        var resourcePack = resourcePack();

        handler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            event.setHardcore(settings.hardcore());
            var address = event.getPlayer().getPlayerConnection().getRemoteAddress();
            String ip = address instanceof java.net.InetSocketAddress inet ? AccessCommands.host(inet) : null;
            var denied = access.deny(event.getPlayer().getUuid(), ip);
            if (denied.isPresent()) {
                event.getPlayer().kick(denied.get());
                return;
            }
            userCache.remember(event.getPlayer().getUuid(), event.getPlayer().getUsername());
            if (MinecraftServer.getConnectionManager().getOnlinePlayers().size() >= settings.maxPlayers()) {
                event.getPlayer().kick(Component.text("The server is full!"));
                return;
            }
            players.wrap(event.getPlayer());
            if (resourcePack != null) {
                players.wrap(event.getPlayer()).sendResourcePack(resourcePack);
            }
            var saved = playerData.spawn(event.getPlayer().getUuid()).flatMap(spawn -> worlds.loadExisting(spawn.world()).map(world -> Map.entry(world, spawn.position())));
            if (saved.isPresent()) {
                event.setSpawningInstance(worlds.instanceOf(saved.get().getKey()));
                event.getPlayer().setRespawnPoint(saved.get().getValue());
                return;
            }
            Location location = worlds.defaultWorld().spawn();
            event.setSpawningInstance(worlds.instanceOf(location.world()));
            event.getPlayer().setRespawnPoint(new Pos(location.x(), location.y(), location.z()));
        });

        handler.addListener(PlayerSpawnEvent.class, event -> {
            if (!event.isFirstSpawn()) {
                return;
            }
            var data = playerData.read(event.getPlayer().getUuid());
            data.ifPresent(tag -> playerData.apply(tag, event.getPlayer(), !settings.forceGamemode()));
            if (data.isEmpty() || settings.forceGamemode()) {
                event.getPlayer().setGameMode(net.minestom.server.entity.GameMode.valueOf(settings.gamemode().name()));
            }
        });

        handler.addListener(PlayerDisconnectEvent.class, event -> savePlayer(event.getPlayer()));

        handler.addListener(PlayerResourcePackStatusEvent.class, event -> {
            var status = event.getStatus();
            events.post(new PlayerResourcePackEvent(players.wrap(event.getPlayer()), event.getPackUuid(), status));
            boolean failed = !status.intermediate() && status != net.kyori.adventure.resource.ResourcePackStatus.SUCCESSFULLY_LOADED;
            if (settings.requireResourcePack() && failed) {
                event.getPlayer().kick(Component.text("This server requires its resource pack"));
            }
        });

        handler.addListener(ServerListPingEvent.class, event -> {
            if (!settings.enableStatus()) {
                event.setCancelled(true);
                return;
            }
            Status status = event.getStatus();
            int online = MinecraftServer.getConnectionManager().getOnlinePlayers().size();
            var ping = events.post(new space.nows.tube.api.event.ServerListPingEvent(
                    event.getConnection() == null ? null : event.getConnection().getRemoteAddress(),
                    status.versionInfo().protocolVersion(), motd, online, settings.maxPlayers(), settings.hideOnlinePlayers()));
            if (ping.isCancelled()) {
                event.setCancelled(true);
                return;
            }
            Status.PlayerInfo playerInfo = ping.hidePlayers()
                    ? new Status.PlayerInfo(ping.online(), ping.maxPlayers())
                    : Status.PlayerInfo.builder(Status.PlayerInfo.online(12)).maxPlayers(ping.maxPlayers()).build();
            event.setStatus(Status.builder(status)
                    .description(ping.motd())
                    .playerInfo(playerInfo)
                    .versionInfo(new Status.VersionInfo(name() + " " + minecraftVersion(), status.versionInfo().protocolVersion()))
                    .build());
        });
    }

    private static Component parseMotd(String text) {
        return text.indexOf('§') >= 0
                ? net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(text)
                : MiniMessage.miniMessage().deserialize(text);
    }

    private space.nows.tube.api.ResourcePack resourcePack() {
        if (settings.resourcePack().isEmpty()) {
            return null;
        }
        try {
            java.net.URI.create(settings.resourcePack());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("server.properties: resource-pack is not a valid URL, no resource pack will be sent");
            return null;
        }
        java.util.UUID id = settings.resourcePackId().isEmpty()
                ? java.util.UUID.nameUUIDFromBytes(settings.resourcePack().getBytes(StandardCharsets.UTF_8))
                : java.util.UUID.fromString(settings.resourcePackId());
        Component prompt = settings.resourcePackPrompt().isEmpty() ? null : parseMotd(settings.resourcePackPrompt());
        return new space.nows.tube.api.ResourcePack(id, settings.resourcePack(), settings.resourcePackSha1(), settings.requireResourcePack(), prompt);
    }

    private void startRemoteAccess() {
        if (settings.enableRcon()) {
            if (settings.rconPassword().isEmpty()) {
                LOGGER.warn("server.properties: enable-rcon is true but rcon.password is empty, RCON stays disabled");
            } else {
                try {
                    rcon = new RconServer(network, new java.net.InetSocketAddress(settings.bindAddress(), settings.rconPort()), settings.rconPassword(), this::runRcon);
                    LOGGER.info("RCON running on {}:{}", settings.bindAddress(), settings.rconPort());
                } catch (RuntimeException e) {
                    LOGGER.error("Could not start RCON on port {}", settings.rconPort(), e);
                }
            }
        }
        if (settings.enableQuery()) {
            try {
                query = new QueryServer(network, new java.net.InetSocketAddress(settings.bindAddress(), settings.queryPort()), this::queryInfo);
                LOGGER.info("Query running on {}:{}", settings.bindAddress(), settings.queryPort());
            } catch (RuntimeException e) {
                LOGGER.error("Could not start Query on port {}", settings.queryPort(), e);
            }
        }
    }

    private CompletableFuture<String> runRcon(String line) {
        CompletableFuture<String> result = new CompletableFuture<>();
        scheduler.runSync(() -> {
            RconSender sender = new RconSender();
            try {
                if (settings.broadcastRconToOps()) {
                    players().stream().filter(player -> permissions.isOp(player.uuid()))
                            .forEach(player -> player.sendMessage(Component.text("[Rcon: " + line + "]", net.kyori.adventure.text.format.NamedTextColor.GRAY)));
                }
                commands.dispatch(sender, line);
                result.complete(sender.output());
            } catch (RuntimeException e) {
                LOGGER.error("RCON command failed: {}", line, e);
                result.completeExceptionally(e);
            }
        });
        return result;
    }

    private QueryServer.Info queryInfo() {
        String pluginList = plugins().stream().map(plugin -> plugin.name() + " " + plugin.version()).collect(java.util.stream.Collectors.joining("; "));
        var online = players();
        return new QueryServer.Info(
                PlainTextComponentSerializer.plainText().serialize(parseMotd(settings.motd())),
                "SMP",
                settings.levelName(),
                minecraftVersion(),
                pluginList.isEmpty() ? name() + " " + version : name() + " " + version + ": " + pluginList,
                online.size(),
                settings.maxPlayers(),
                settings.serverPort(),
                settings.bindAddress(),
                online.stream().map(Player::name).toList());
    }

    private void installServerListeners() {
        events.subscribe(BlockBreakEvent.class, EventPriority.LOW, true, event -> protectSpawn(event.player(), event.world(), event.x(), event.z(), event));
        events.subscribe(BlockPlaceEvent.class, EventPriority.LOW, true, event -> protectSpawn(event.player(), event.world(), event.x(), event.z(), event));

        events.subscribe(PlayerJoinEvent.class, EventPriority.MONITOR, false, event -> {
            String where = settings.logIps() ? event.player().address().map(address -> "/" + AccessCommands.host(address)).orElse("local") : "local";
            LOGGER.info("{}[{}] logged in", event.player().name(), where);
            if (event.joinMessage() != null) {
                LOGGER.info("{} joined the game", event.player().name());
            }
        });
        events.subscribe(PlayerQuitEvent.class, EventPriority.MONITOR, false, event -> {
            if (event.quitMessage() != null) {
                LOGGER.info("{} left the game", event.player().name());
            }
        });
    }

    private void savePlayer(net.minestom.server.entity.Player player) {
        if (player.getInstance() == null) {
            return;
        }
        try {
            playerData.save(player, worlds.of(player.getInstance()).name());
        } catch (RuntimeException e) {
            LOGGER.error("Could not save the data of {}", player.getUsername(), e);
        }
    }

    private void protectSpawn(Player player, space.nows.tube.api.World world, int x, int z, space.nows.tube.api.event.Cancellable event) {
        int radius = settings.spawnProtection();
        if (radius <= 0 || !world.equals(worlds.defaultWorld()) || permissions.isOp(player.uuid())) {
            return;
        }
        var spawn = world.spawn();
        if (Math.abs(x - (int) spawn.x()) <= radius && Math.abs(z - (int) spawn.z()) <= radius) {
            event.setCancelled(true);
            player.sendMessage("You cannot modify the area around the spawn point");
        }
    }

    private void enableExtensions() {
        for (ServerExtension extension : java.util.ServiceLoader.load(ServerExtension.class, ServerExtension.class.getClassLoader())) {
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
        if (autosave != null) {
            autosave.shutdownNow();
        }
        extensions.reversed().forEach(extension -> {
            try {
                extension.disable();
            } catch (RuntimeException e) {
                LOGGER.error("Extension {} failed to disable", extension.name(), e);
            }
        });
        plugins.disableAll();
        if (rcon != null) {
            rcon.close();
        }
        if (query != null) {
            query.close();
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

    private void startAutosave() {
        if (settings.autosaveSeconds() <= 0) {
            return;
        }
        autosave = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "tube-autosave");
            thread.setDaemon(true);
            return thread;
        });
        autosave.scheduleWithFixedDelay(() -> {
            try {
                worlds.saveAll();
                scheduler.runSync(() -> MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(this::savePlayer));
            } catch (RuntimeException e) {
                LOGGER.error("Autosave failed", e);
            }
        }, settings.autosaveSeconds(), settings.autosaveSeconds(), TimeUnit.SECONDS);
    }

    private void dispatchConsole(String line) {
        try {
            commands.dispatchConsole(line);
        } catch (RuntimeException e) {
            LOGGER.error("Command failed", e);
        }
    }

    private void startConsole() {
        Thread thread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String input = line;
                    scheduler.runSync(() -> dispatchConsole(input));
                }
            } catch (IOException e) {
                LOGGER.warn("Console input closed", e);
            }
        }, "tube-console");
        thread.setDaemon(true);
        thread.start();
    }

    private static String readVersion() {
        try (InputStream in = TubeServer.class.getResourceAsStream("/tube-server.properties")) {
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

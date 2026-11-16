package space.nows.tube.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.ping.Status;
import net.minestom.server.world.Difficulty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.CommandRegistry;
import space.nows.tube.api.Location;
import space.nows.tube.api.PermissionManager;
import space.nows.tube.api.Player;
import space.nows.tube.api.PluginDescriptor;
import space.nows.tube.api.Scheduler;
import space.nows.tube.api.Server;
import space.nows.tube.api.WorldManager;
import space.nows.tube.api.event.EventBus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.Properties;
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
    private WorldService worlds;
    private PlayerService players;
    private CommandService commands;
    private SchedulerService scheduler;
    private DefaultEventBus events;
    private PluginManager plugins;
    private ScheduledExecutorService autosave;

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
    public void broadcast(Component message) {
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> player.sendMessage(message));
        LOGGER.info(PlainTextComponentSerializer.plainText().serialize(message));
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

        MinecraftServer server = MinecraftServer.init(settings.onlineMode() ? new Auth.Online() : new Auth.Offline());
        MinecraftServer.setBrandName(name());
        MinecraftServer.setDifficulty(Difficulty.valueOf(settings.difficulty().name()));

        permissions = new PermissionService(directory);
        worlds = new WorldService(directory.resolve("worlds"), settings.levelName());
        players = new PlayerService(worlds, permissions);
        commands = new CommandService(players);
        scheduler = new SchedulerService();
        events = new DefaultEventBus();
        plugins = new PluginManager(this, commands, scheduler, events, directory.resolve("plugins"));

        installConnectionHandlers();
        new EventBridge(events, players, worlds).install(MinecraftServer.getGlobalEventHandler());

        BuiltinCommands.register(this, commands);
        worlds.defaultWorld();
        plugins.loadAll();

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "tube-shutdown"));
        startAutosave();
        startConsole();
        server.start(settings.bindAddress(), settings.serverPort());
    }

    private void installConnectionHandlers() {
        var handler = MinecraftServer.getGlobalEventHandler();
        Component motd = MiniMessage.miniMessage().deserialize(settings.motd());

        handler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            if (MinecraftServer.getConnectionManager().getOnlinePlayers().size() >= settings.maxPlayers()) {
                event.getPlayer().kick(Component.text("The server is full!"));
                return;
            }
            players.wrap(event.getPlayer());
            Location location = worlds.defaultWorld().spawn();
            event.setSpawningInstance(worlds.instanceOf(location.world()));
            event.getPlayer().setRespawnPoint(new Pos(location.x(), location.y(), location.z()));
        });

        handler.addListener(PlayerSpawnEvent.class, event -> {
            if (event.isFirstSpawn()) {
                event.getPlayer().setGameMode(net.minestom.server.entity.GameMode.valueOf(settings.gamemode().name()));
            }
        });

        handler.addListener(ServerListPingEvent.class, event -> {
            Status status = event.getStatus();
            event.setStatus(Status.builder(status)
                    .description(motd)
                    .playerInfo(MinecraftServer.getConnectionManager().getOnlinePlayers().size(), settings.maxPlayers())
                    .versionInfo(new Status.VersionInfo(name() + " " + minecraftVersion(), status.versionInfo().protocolVersion()))
                    .build());
        });
    }

    private void shutdown() {
        if (!stopped.compareAndSet(false, true)) {
            return;
        }
        LOGGER.info("Stopping server");
        if (autosave != null) {
            autosave.shutdownNow();
        }
        plugins.disableAll();
        try {
            worlds.saveAll();
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

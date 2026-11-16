package space.nows.tube.server;

import net.kyori.adventure.text.Component;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.CommandRegistry;
import space.nows.tube.api.Location;
import space.nows.tube.api.Player;
import space.nows.tube.api.PluginDescriptor;
import space.nows.tube.api.Scheduler;
import space.nows.tube.api.Server;
import space.nows.tube.api.WorldManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TubeServer implements Server {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");

    private final VersionProfile profile;
    private final Path directory;
    private final AtomicBoolean stopped = new AtomicBoolean();

    private ServerConfig config;
    private WorldService worlds;
    private PlayerService players;
    private CommandService commands;
    private SchedulerService scheduler;
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
    public String minecraftVersion() {
        return profile.minecraftVersion();
    }

    @Override
    public CommandRegistry commands() {
        return commands;
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
    public Collection<Player> players() {
        return players.online();
    }

    @Override
    public Collection<PluginDescriptor> plugins() {
        return plugins.descriptors();
    }

    @Override
    public void broadcast(String message) {
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> player.sendMessage(Component.text(message)));
        LOGGER.info(message);
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
        config = ServerConfig.load(directory.resolve("tube.yml"));

        MinecraftServer server = MinecraftServer.init(config.onlineMode() ? new Auth.Online() : new Auth.Offline());
        MinecraftServer.setBrandName(name());

        worlds = new WorldService(directory.resolve("worlds"), config.defaultWorld());
        players = new PlayerService(worlds);
        commands = new CommandService(players);
        scheduler = new SchedulerService();
        plugins = new PluginManager(this, commands, scheduler, directory.resolve("plugins"));

        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, event -> {
            players.wrap(event.getPlayer());
            Location location = worlds.defaultWorld().spawn();
            event.setSpawningInstance(worlds.instanceOf(location.world()));
            event.getPlayer().setRespawnPoint(new Pos(location.x(), location.y(), location.z()));
        });
        MinecraftServer.getGlobalEventHandler().addListener(PlayerDisconnectEvent.class, event -> players.remove(event.getPlayer()));

        BuiltinCommands.register(this, commands);
        worlds.defaultWorld();
        plugins.loadAll();

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "tube-shutdown"));
        startAutosave();
        startConsole();
        server.start(config.host(), config.port());
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
        if (config.autosaveSeconds() <= 0) {
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
        }, config.autosaveSeconds(), config.autosaveSeconds(), TimeUnit.SECONDS);
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
}

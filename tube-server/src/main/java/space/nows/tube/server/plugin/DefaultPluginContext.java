package space.nows.tube.server.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.CommandRegistry;
import space.nows.tube.api.PluginConfig;
import space.nows.tube.api.PluginContext;
import space.nows.tube.api.PluginDescriptor;
import space.nows.tube.api.Scheduler;
import space.nows.tube.api.Server;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.network.Network;
import space.nows.tube.server.config.ConfigFormat;
import space.nows.tube.server.config.FileConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

final class DefaultPluginContext implements PluginContext {

    private static final String CONFIG_NAME = "config";

    private final Server server;
    private final PluginDescriptor descriptor;
    private final Path jar;
    private final Path dataFolder;
    private final CommandRegistry commands;
    private final Scheduler scheduler;
    private final EventBus events;
    private final Network network;
    private final Logger logger;
    private PluginConfig config;

    DefaultPluginContext(Server server, PluginDescriptor descriptor, Path jar, Path dataFolder, CommandRegistry commands, Scheduler scheduler, EventBus events, Network network) {
        this.server = server;
        this.descriptor = descriptor;
        this.jar = jar;
        this.dataFolder = dataFolder;
        this.commands = commands;
        this.scheduler = scheduler;
        this.events = events;
        this.network = network;
        this.logger = LoggerFactory.getLogger(descriptor.name());
    }

    @Override
    public Server server() {
        return server;
    }

    @Override
    public PluginDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public Path dataFolder() {
        return dataFolder;
    }

    @Override
    public Logger logger() {
        return logger;
    }

    @Override
    public CommandRegistry commands() {
        return commands;
    }

    @Override
    public EventBus events() {
        return events;
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
    public synchronized PluginConfig config() {
        if (config == null) {
            config = FileConfig.load(configFile());
        }
        return config;
    }

    private Path configFile() {
        for (ConfigFormat format : ConfigFormat.values()) {
            for (String extension : format.extensions()) {
                Path existing = dataFolder.resolve(CONFIG_NAME + "." + extension);
                if (Files.exists(existing)) {
                    return existing;
                }
            }
        }
        try (JarFile file = new JarFile(jar.toFile())) {
            JarEntry entry = Jars.findByBaseName(file, CONFIG_NAME);
            if (entry == null) {
                return dataFolder.resolve(CONFIG_NAME + ".yml");
            }
            Path target = dataFolder.resolve(entry.getName());
            Files.createDirectories(dataFolder);
            try (InputStream in = file.getInputStream(entry)) {
                Files.copy(in, target);
            }
            return target;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

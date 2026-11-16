package space.nows.tube.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.CommandRegistry;
import space.nows.tube.api.PluginConfig;
import space.nows.tube.api.PluginContext;
import space.nows.tube.api.PluginDescriptor;
import space.nows.tube.api.Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;

final class DefaultPluginContext implements PluginContext {

    private static final String CONFIG_FILE = "config.yml";

    private final Server server;
    private final PluginDescriptor descriptor;
    private final Path jar;
    private final Path dataFolder;
    private final CommandRegistry commands;
    private final Logger logger;
    private PluginConfig config;

    DefaultPluginContext(Server server, PluginDescriptor descriptor, Path jar, Path dataFolder, CommandRegistry commands) {
        this.server = server;
        this.descriptor = descriptor;
        this.jar = jar;
        this.dataFolder = dataFolder;
        this.commands = commands;
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
    public synchronized PluginConfig config() {
        if (config == null) {
            Path file = dataFolder.resolve(CONFIG_FILE);
            if (!Files.exists(file)) {
                copyDefault(file);
            }
            config = YamlConfig.load(file);
        }
        return config;
    }

    private void copyDefault(Path target) {
        try (JarFile file = new JarFile(jar.toFile())) {
            var entry = file.getEntry(CONFIG_FILE);
            if (entry == null) {
                return;
            }
            Files.createDirectories(dataFolder);
            try (InputStream in = file.getInputStream(entry)) {
                Files.copy(in, target);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

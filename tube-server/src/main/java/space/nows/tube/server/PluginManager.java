package space.nows.tube.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.CommandRegistry;
import space.nows.tube.api.Plugin;
import space.nows.tube.api.PluginDescriptor;
import space.nows.tube.api.Scheduler;
import space.nows.tube.api.Server;
import space.nows.tube.api.event.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class PluginManager {

    static final String DESCRIPTOR_NAME = "tube-plugin";

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");
    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9_.-]+");

    private record Candidate(Path jar, PluginDescriptor descriptor) {
    }

    private record Running(PluginDescriptor descriptor, Plugin plugin, URLClassLoader loader, ScopedCommands commands, ScopedScheduler scheduler, ScopedEvents events) {
    }

    private final Server server;
    private final CommandRegistry commands;
    private final Scheduler scheduler;
    private final EventBus events;
    private final Path directory;
    private final List<Running> running = new ArrayList<>();

    public PluginManager(Server server, CommandRegistry commands, Scheduler scheduler, EventBus events, Path directory) {
        this.server = server;
        this.commands = commands;
        this.scheduler = scheduler;
        this.events = events;
        this.directory = directory;
    }

    public synchronized void loadAll() {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
        }

        Map<String, Candidate> candidates = discover();
        List<Candidate> ordered = order(candidates);
        for (Candidate candidate : ordered) {
            enable(candidate);
        }
        LOGGER.info("Enabled {} plugin(s)", running.size());
    }

    public synchronized void disableAll() {
        for (Running plugin : running.reversed()) {
            try {
                plugin.plugin().onDisable();
            } catch (Throwable e) {
                LOGGER.error("Plugin {} failed to disable", plugin.descriptor().name(), e);
            }
            plugin.commands().unregisterAll();
            plugin.scheduler().cancelAll();
            plugin.events().cancelAll();
            try {
                plugin.loader().close();
            } catch (IOException e) {
                LOGGER.warn("Could not close class loader of {}", plugin.descriptor().name(), e);
            }
        }
        running.clear();
    }

    public synchronized Collection<PluginDescriptor> descriptors() {
        return running.stream().map(Running::descriptor).toList();
    }

    private Map<String, Candidate> discover() {
        Map<String, Candidate> found = new LinkedHashMap<>();
        List<Path> jars;
        try (Stream<Path> files = Files.list(directory)) {
            jars = files.filter(path -> path.getFileName().toString().endsWith(".jar")).sorted().toList();
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
        for (Path jar : jars) {
            try {
                PluginDescriptor descriptor = readDescriptor(jar);
                if (found.containsKey(descriptor.name())) {
                    LOGGER.error("Skipping {}: plugin {} is already provided by {}", jar.getFileName(), descriptor.name(), found.get(descriptor.name()).jar().getFileName());
                    continue;
                }
                found.put(descriptor.name(), new Candidate(jar, descriptor));
            } catch (Exception e) {
                if (!hasBukkitDescriptor(jar)) {
                    LOGGER.error("Skipping {}: {}", jar.getFileName(), e.getMessage());
                }
            }
        }
        return found;
    }

    private static boolean hasBukkitDescriptor(Path jar) {
        try (JarFile file = new JarFile(jar.toFile())) {
            return file.getEntry("plugin.yml") != null || file.getEntry("paper-plugin.yml") != null;
        } catch (IOException e) {
            return false;
        }
    }

    static PluginDescriptor readDescriptor(Path jar) throws IOException {
        try (JarFile file = new JarFile(jar.toFile())) {
            var entry = Jars.findByBaseName(file, DESCRIPTOR_NAME);
            if (entry == null) {
                throw new IllegalArgumentException("missing " + DESCRIPTOR_NAME + ".yml, .json or .toml");
            }
            Map<String, Object> map;
            try (InputStream in = file.getInputStream(entry)) {
                String text = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                map = ConfigFormat.ofFileName(entry.getName()).orElseThrow().parse(text);
            }
            String name = required(map, "name");
            if (!VALID_NAME.matcher(name).matches()) {
                throw new IllegalArgumentException("invalid plugin name " + name);
            }
            List<String> depends = map.get("depends") instanceof List<?> list ? list.stream().map(String::valueOf).toList() : List.of();
            return new PluginDescriptor(name, required(map, "version"), required(map, "main"), depends);
        }
    }

    private static String required(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            throw new IllegalArgumentException(DESCRIPTOR_NAME + " is missing " + key);
        }
        return String.valueOf(value);
    }

    private List<Candidate> order(Map<String, Candidate> candidates) {
        List<Candidate> ordered = new ArrayList<>();
        Map<String, Boolean> state = new HashMap<>();
        for (String name : candidates.keySet()) {
            visit(name, candidates, state, ordered);
        }
        return ordered;
    }

    private boolean visit(String name, Map<String, Candidate> candidates, Map<String, Boolean> state, List<Candidate> ordered) {
        Boolean done = state.get(name);
        if (done != null) {
            if (!done && !isOrdered(name, ordered)) {
                LOGGER.error("Plugin {} is part of a dependency cycle", name);
                state.put(name, false);
                return false;
            }
            return done;
        }
        Candidate candidate = candidates.get(name);
        if (candidate == null) {
            return false;
        }
        state.put(name, false);
        for (String dependency : candidate.descriptor().depends()) {
            if (!candidates.containsKey(dependency)) {
                LOGGER.error("Plugin {} requires missing plugin {}", name, dependency);
                return false;
            }
            if (!visit(dependency, candidates, state, ordered)) {
                LOGGER.error("Plugin {} cannot load because {} is unavailable", name, dependency);
                return false;
            }
        }
        ordered.add(candidate);
        state.put(name, true);
        return true;
    }

    private static boolean isOrdered(String name, List<Candidate> ordered) {
        return ordered.stream().anyMatch(candidate -> candidate.descriptor().name().equals(name));
    }

    private void enable(Candidate candidate) {
        PluginDescriptor descriptor = candidate.descriptor();
        for (String dependency : descriptor.depends()) {
            if (running.stream().noneMatch(plugin -> plugin.descriptor().name().equals(dependency))) {
                LOGGER.error("Plugin {} not enabled: dependency {} failed to enable", descriptor.name(), dependency);
                return;
            }
        }

        ScopedCommands scoped = new ScopedCommands(commands);
        ScopedScheduler scopedScheduler = new ScopedScheduler(scheduler);
        ScopedEvents scopedEvents = new ScopedEvents(events);
        URLClassLoader loader = null;
        try {
            URL url = candidate.jar().toUri().toURL();
            loader = new URLClassLoader(new URL[] {url}, PluginManager.class.getClassLoader());
            Class<?> type = Class.forName(descriptor.main(), true, loader);
            if (!Plugin.class.isAssignableFrom(type)) {
                throw new IllegalStateException(descriptor.main() + " does not implement Plugin");
            }
            Plugin plugin = (Plugin) type.getDeclaredConstructor().newInstance();
            Path dataFolder = directory.resolve(descriptor.name());
            plugin.onEnable(new DefaultPluginContext(server, descriptor, candidate.jar(), dataFolder, scoped, scopedScheduler, scopedEvents));
            running.add(new Running(descriptor, plugin, loader, scoped, scopedScheduler, scopedEvents));
            LOGGER.info("Enabled {} {}", descriptor.name(), descriptor.version());
        } catch (Throwable e) {
            LOGGER.error("Could not enable {}", descriptor.name(), e);
            scoped.unregisterAll();
            scopedScheduler.cancelAll();
            scopedEvents.cancelAll();
            if (loader != null) {
                try {
                    loader.close();
                } catch (IOException closeFailure) {
                    LOGGER.warn("Could not close class loader of {}", descriptor.name(), closeFailure);
                }
            }
        }
    }
}

package space.nows.tube.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.api.Command;
import space.nows.tube.api.CommandRegistry;
import space.nows.tube.api.PluginDescriptor;
import space.nows.tube.api.ScheduledTask;
import space.nows.tube.api.Scheduler;
import space.nows.tube.api.Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

class PluginManagerTest {

    private static final class MapRegistry implements CommandRegistry {

        private final Set<String> names = new TreeSet<>();

        @Override
        public void register(String name, Command command, String... aliases) {
            names.add(name.toLowerCase(Locale.ROOT));
        }

        @Override
        public void unregister(String name) {
            names.remove(name.toLowerCase(Locale.ROOT));
        }

        @Override
        public Collection<String> names() {
            return names;
        }
    }

    @TempDir
    Path directory;

    private final MapRegistry registry = new MapRegistry();
    private final Scheduler scheduler = new Scheduler() {
        @Override
        public ScheduledTask runSync(Runnable task) {
            return null;
        }

        @Override
        public ScheduledTask runLater(long delayTicks, Runnable task) {
            return null;
        }

        @Override
        public ScheduledTask runRepeating(long delayTicks, long periodTicks, Runnable task) {
            return null;
        }
    };
    private final Server server = (Server) Proxy.newProxyInstance(
            Server.class.getClassLoader(), new Class<?>[] {Server.class}, (proxy, method, args) -> null);

    @BeforeEach
    void clearEvents() {
        RecordingPlugin.EVENTS.clear();
    }

    @Test
    void enablesInDependencyOrderAndDisablesInReverse() throws IOException {
        writePlugin("1-first.jar", "b", List.of("a"), null);
        writePlugin("2-second.jar", "a", List.of(), "greeting: hello\n");

        PluginManager manager = new PluginManager(server, registry, scheduler, directory);
        manager.loadAll();

        assertEquals(List.of("enable:a:hello", "enable:b:none"), RecordingPlugin.EVENTS);
        assertEquals(Set.of("cmd-a", "cmd-b"), Set.copyOf(registry.names()));
        assertEquals(List.of("a", "b"), manager.descriptors().stream().map(PluginDescriptor::name).toList());

        RecordingPlugin.EVENTS.clear();
        manager.disableAll();

        assertEquals(List.of("disable:b", "disable:a"), RecordingPlugin.EVENTS);
        assertTrue(registry.names().isEmpty());
        assertTrue(manager.descriptors().isEmpty());
    }

    @Test
    void copiesDefaultConfigIntoDataFolder() throws IOException {
        writePlugin("a.jar", "a", List.of(), "greeting: hello\n");
        new PluginManager(server, registry, scheduler, directory).loadAll();
        assertTrue(Files.isRegularFile(directory.resolve("a").resolve("config.yml")));
    }

    @Test
    void readsDescriptorAndConfigInJsonAndToml() throws IOException {
        writePlugin("json.jar", "j", List.of(), "{\"greeting\": \"from-json\"}", "config.json", "json");
        writePlugin("toml.jar", "t", List.of("j"), "greeting = \"from-toml\"\n", "config.toml", "toml");
        new PluginManager(server, registry, scheduler, directory).loadAll();
        assertEquals(List.of("enable:j:from-json", "enable:t:from-toml"), RecordingPlugin.EVENTS);
        assertTrue(Files.isRegularFile(directory.resolve("t").resolve("config.toml")));
    }

    @Test
    void skipsPluginWithMissingDependency() throws IOException {
        writePlugin("b.jar", "b", List.of("missing"), null);
        PluginManager manager = new PluginManager(server, registry, scheduler, directory);
        manager.loadAll();
        assertTrue(RecordingPlugin.EVENTS.isEmpty());
        assertTrue(manager.descriptors().isEmpty());
    }

    @Test
    void skipsJarWithoutDescriptor() throws IOException {
        try (JarOutputStream ignored = new JarOutputStream(Files.newOutputStream(directory.resolve("broken.jar")))) {
            ignored.flush();
        }
        PluginManager manager = new PluginManager(server, registry, scheduler, directory);
        manager.loadAll();
        assertTrue(manager.descriptors().isEmpty());
    }

    private void writePlugin(String fileName, String name, List<String> depends, String config) throws IOException {
        writePlugin(fileName, name, depends, config, "config.yml", "yml");
    }

    private void writePlugin(String fileName, String name, List<String> depends, String config, String configName, String descriptorExtension) throws IOException {
        String main = RecordingPlugin.class.getName();
        String quoted = depends.stream().map(item -> "\"" + item + "\"").collect(java.util.stream.Collectors.joining(", "));
        String descriptor = switch (descriptorExtension) {
            case "json" -> "{\"name\": \"" + name + "\", \"version\": \"1.0\", \"main\": \"" + main + "\", \"depends\": [" + quoted + "]}";
            case "toml" -> "name = \"" + name + "\"\nversion = \"1.0\"\nmain = \"" + main + "\"\ndepends = [" + quoted + "]\n";
            default -> "name: " + name + "\nversion: 1.0\nmain: " + main + "\ndepends: [" + quoted + "]\n";
        };
        try (OutputStream out = Files.newOutputStream(directory.resolve(fileName)); JarOutputStream jar = new JarOutputStream(out)) {
            jar.putNextEntry(new JarEntry("tube-plugin." + descriptorExtension));
            jar.write(descriptor.getBytes(StandardCharsets.UTF_8));
            jar.closeEntry();
            if (config != null) {
                jar.putNextEntry(new JarEntry(configName));
                jar.write(config.getBytes(StandardCharsets.UTF_8));
                jar.closeEntry();
            }
            String classFile = RecordingPlugin.class.getName().replace('.', '/') + ".class";
            try (InputStream in = RecordingPlugin.class.getClassLoader().getResourceAsStream(classFile)) {
                jar.putNextEntry(new JarEntry(classFile));
                jar.write(in.readAllBytes());
                jar.closeEntry();
            }
        }
    }
}

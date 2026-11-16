package space.nows.tube.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.api.Command;
import space.nows.tube.api.CommandRegistry;
import space.nows.tube.api.PluginDescriptor;
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

        PluginManager manager = new PluginManager(server, registry, directory);
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
        new PluginManager(server, registry, directory).loadAll();
        assertTrue(Files.isRegularFile(directory.resolve("a").resolve("config.yml")));
    }

    @Test
    void skipsPluginWithMissingDependency() throws IOException {
        writePlugin("b.jar", "b", List.of("missing"), null);
        PluginManager manager = new PluginManager(server, registry, directory);
        manager.loadAll();
        assertTrue(RecordingPlugin.EVENTS.isEmpty());
        assertTrue(manager.descriptors().isEmpty());
    }

    @Test
    void skipsJarWithoutDescriptor() throws IOException {
        try (JarOutputStream ignored = new JarOutputStream(Files.newOutputStream(directory.resolve("broken.jar")))) {
            ignored.flush();
        }
        PluginManager manager = new PluginManager(server, registry, directory);
        manager.loadAll();
        assertTrue(manager.descriptors().isEmpty());
    }

    private void writePlugin(String fileName, String name, List<String> depends, String config) throws IOException {
        String descriptor = "name: " + name + "\nversion: 1.0\nmain: " + RecordingPlugin.class.getName() + "\ndepends: " + depends + "\n";
        try (OutputStream out = Files.newOutputStream(directory.resolve(fileName)); JarOutputStream jar = new JarOutputStream(out)) {
            jar.putNextEntry(new JarEntry(PluginManager.DESCRIPTOR_FILE));
            jar.write(descriptor.getBytes(StandardCharsets.UTF_8));
            jar.closeEntry();
            if (config != null) {
                jar.putNextEntry(new JarEntry("config.yml"));
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

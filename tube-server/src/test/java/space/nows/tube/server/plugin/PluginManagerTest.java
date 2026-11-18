package space.nows.tube.server.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.api.Server;
import space.nows.tube.api.command.Command;
import space.nows.tube.api.command.CommandInfo;
import space.nows.tube.api.command.CommandRegistry;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.plugin.PluginDescriptor;
import space.nows.tube.api.scheduler.ScheduledTask;
import space.nows.tube.api.scheduler.Scheduler;
import space.nows.tube.server.access.PermissionService;
import space.nows.tube.server.event.DefaultEventBus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

class PluginManagerTest {

    private static final class MapRegistry implements CommandRegistry {

        private final Set<String> names = new TreeSet<>();
        private final Map<String, Command> commands = new HashMap<>();
        private final Map<String, List<String>> aliases = new HashMap<>();

        @Override
        public void register(String name, Command command, String... aliases) {
            names.add(name.toLowerCase(Locale.ROOT));
            commands.put(name.toLowerCase(Locale.ROOT), command);
            this.aliases.put(name.toLowerCase(Locale.ROOT), List.of(aliases));
        }

        @Override
        public void unregister(String name) {
            names.remove(name.toLowerCase(Locale.ROOT));
            commands.remove(name.toLowerCase(Locale.ROOT));
            aliases.remove(name.toLowerCase(Locale.ROOT));
        }

        @Override
        public Collection<String> names() {
            return names;
        }

        @Override
        public Optional<CommandInfo> find(String label) {
            String key = label.toLowerCase(Locale.ROOT);
            boolean known = names.contains(key) || aliases.values().stream().anyMatch(list -> list.contains(key));
            return known ? Optional.of(new CommandInfo(key, List.of(), null, null, null)) : Optional.empty();
        }

        @Override
        public boolean dispatch(CommandSender sender, String line) {
            return false;
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

        PluginManager manager = new PluginManager(server, registry, scheduler, new DefaultEventBus(), directory);
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
        new PluginManager(server, registry, scheduler, new DefaultEventBus(), directory).loadAll();
        assertTrue(Files.isRegularFile(directory.resolve("a").resolve("config.yml")));
    }

    @Test
    void readsDescriptorAndConfigInJsonAndToml() throws IOException {
        writePlugin("json.jar", "j", List.of(), "{\"greeting\": \"from-json\"}", "config.json", "json");
        writePlugin("toml.jar", "t", List.of("j"), "greeting = \"from-toml\"\n", "config.toml", "toml");
        new PluginManager(server, registry, scheduler, new DefaultEventBus(), directory).loadAll();
        assertEquals(List.of("enable:j:from-json", "enable:t:from-toml"), RecordingPlugin.EVENTS);
        assertTrue(Files.isRegularFile(directory.resolve("t").resolve("config.toml")));
    }

    @Test
    void skipsPluginWithMissingDependency() throws IOException {
        writePlugin("b.jar", "b", List.of("missing"), null);
        PluginManager manager = new PluginManager(server, registry, scheduler, new DefaultEventBus(), directory);
        manager.loadAll();
        assertTrue(RecordingPlugin.EVENTS.isEmpty());
        assertTrue(manager.descriptors().isEmpty());
    }

    @Test
    void skipsJarWithoutDescriptor() throws IOException {
        try (JarOutputStream ignored = new JarOutputStream(Files.newOutputStream(directory.resolve("broken.jar")))) {
            ignored.flush();
        }
        PluginManager manager = new PluginManager(server, registry, scheduler, new DefaultEventBus(), directory);
        manager.loadAll();
        assertTrue(manager.descriptors().isEmpty());
    }

    @Test
    void ordersBySoftDependsAndLoadBefore() throws IOException {
        writeRaw("a.jar", "name: a\nversion: 1\nmain: " + RecordingPlugin.class.getName() + "\nsoftdepend: [b, missing]\n");
        writeRaw("b.jar", "name: b\nversion: 1\nmain: " + RecordingPlugin.class.getName() + "\n");
        writeRaw("c.jar", "name: c\nversion: 1\nmain: " + RecordingPlugin.class.getName() + "\nload-before: [b]\n");
        new PluginManager(server, registry, scheduler, new DefaultEventBus(), directory).loadAll();
        assertEquals(List.of("enable:c:none", "enable:b:none", "enable:a:none"), RecordingPlugin.EVENTS);
    }

    @Test
    void refusesPluginsForANewerTube() throws IOException {
        writeRaw("new.jar", "name: fresh\nversion: 1\nmain: " + RecordingPlugin.class.getName() + "\ntube: '0.2701'\n");
        writeRaw("old.jar", "name: settled\nversion: 1\nmain: " + RecordingPlugin.class.getName() + "\ntube: '0.2611'\nminecraft: ['1.21']\n");
        PluginManager manager = new PluginManager(serverWith(new PermissionService(directory)), registry, scheduler, new DefaultEventBus(), directory);
        manager.loadAll();
        assertEquals(List.of("settled"), manager.descriptors().stream().map(PluginDescriptor::name).toList());
    }

    @Test
    void appliesDeclaredCommandsAndPermissions() throws IOException {
        writeRaw("a.jar", """
                name: a
                version: 1
                main: %s
                commands:
                  cmd-a:
                    description: Does a
                    usage: /<command> <thing>
                    aliases: [ca]
                    permission: a.use
                  never-registered:
                permissions:
                  a.use:
                    default: true
                """.formatted(RecordingPlugin.class.getName()));
        PermissionService permissions = new PermissionService(directory);
        PluginManager manager = new PluginManager(serverWith(permissions), registry, scheduler, new DefaultEventBus(), directory);
        manager.loadAll();

        Command command = registry.commands.get("cmd-a");
        assertEquals("a.use", command.permission());
        assertEquals("Does a", command.description());
        assertEquals("/<command> <thing>", command.usage());
        assertEquals(List.of("ca"), registry.aliases.get("cmd-a"));
        assertTrue(permissions.has(UUID.randomUUID(), "a.use"));

        manager.disableAll();
        assertFalse(permissions.has(UUID.randomUUID(), "a.use"));
    }

    private Server serverWith(PermissionService permissions) {
        return (Server) Proxy.newProxyInstance(Server.class.getClassLoader(), new Class<?>[] {Server.class}, (proxy, method, args) -> switch (method.getName()) {
            case "version" -> "0.2611.2-SNAPSHOT";
            case "minecraftVersion" -> "26.2";
            case "permissions" -> permissions;
            default -> null;
        });
    }

    private void writeRaw(String fileName, String descriptor) throws IOException {
        try (OutputStream out = Files.newOutputStream(directory.resolve(fileName)); JarOutputStream jar = new JarOutputStream(out)) {
            jar.putNextEntry(new JarEntry("tube-plugin.yml"));
            jar.write(descriptor.getBytes(StandardCharsets.UTF_8));
            jar.closeEntry();
            String classFile = RecordingPlugin.class.getName().replace('.', '/') + ".class";
            try (InputStream in = RecordingPlugin.class.getClassLoader().getResourceAsStream(classFile)) {
                jar.putNextEntry(new JarEntry(classFile));
                jar.write(in.readAllBytes());
                jar.closeEntry();
            }
        }
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

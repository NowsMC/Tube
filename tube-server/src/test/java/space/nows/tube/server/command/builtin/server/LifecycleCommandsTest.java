package space.nows.tube.server.command.builtin.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.api.Server;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.event.Event;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.event.server.ServerReloadEvent;
import space.nows.tube.server.AutosaveGate;
import space.nows.tube.server.Restarter;
import space.nows.tube.server.access.BanList;
import space.nows.tube.server.access.PermissionService;
import space.nows.tube.server.access.Whitelist;
import space.nows.tube.server.command.CommandService;
import space.nows.tube.server.command.builtin.BuiltinCommands;
import space.nows.tube.server.player.IdleKicker;
import space.nows.tube.server.player.PlayerService;
import space.nows.tube.server.world.WorldService;

import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class LifecycleCommandsTest {

    @TempDir
    static Path directory;

    private static final List<Event> POSTED = new ArrayList<>();
    private static final AtomicInteger PLUGIN_RELOADS = new AtomicInteger();
    private static final AtomicInteger RESTARTS = new AtomicInteger();
    private static volatile boolean restartable;
    private static CommandService commands;

    @BeforeAll
    static void register() {
        MinecraftServer.init(new Auth.Offline());
        WorldService worlds = new WorldService(directory, "main");
        commands = new CommandService(new PlayerService(worlds, new PermissionService(directory)));
        EventBus events = (EventBus) Proxy.newProxyInstance(EventBus.class.getClassLoader(), new Class<?>[] {EventBus.class}, (proxy, method, args) -> {
            if (method.getName().equals("post")) {
                POSTED.add((Event) args[0]);
                return args[0];
            }
            return null;
        });
        Server server = (Server) Proxy.newProxyInstance(Server.class.getClassLoader(), new Class<?>[] {Server.class}, (proxy, method, args) -> switch (method.getName()) {
            case "name" -> "Tube";
            case "version" -> "1.2.3";
            case "minecraftVersion" -> "26.2";
            case "dataVersion" -> 4999;
            case "players", "plugins" -> List.of();
            case "events" -> events;
            case "worlds" -> worlds;
            default -> null;
        });
        Restarter restarter = new Restarter() {
            @Override
            public boolean available() {
                return restartable;
            }

            @Override
            public void restart() {
                RESTARTS.incrementAndGet();
            }
        };
        ServerControls controls = new ServerControls(new AutosaveGate(), new IdleKicker(0, System::currentTimeMillis), new Whitelist(directory, false),
                new BanList(directory), PLUGIN_RELOADS::incrementAndGet, restarter);
        new BuiltinCommands(server, commands, null, null, null, null, false, controls).registerAll();
    }

    private static List<String> run(String line) {
        List<String> messages = new ArrayList<>();
        CommandSender sender = (CommandSender) Proxy.newProxyInstance(CommandSender.class.getClassLoader(), new Class<?>[] {CommandSender.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "hasPermission" -> {
                            return true;
                        }
                        case "name" -> {
                            return "test";
                        }
                        case "sendMessage" -> {
                            messages.add(args[0] instanceof Component component ? PlainTextComponentSerializer.plainText().serialize(component) : String.valueOf(args[0]));
                            return null;
                        }
                        default -> {
                            return null;
                        }
                    }
                });
        commands.dispatch(sender, line);
        return messages;
    }

    @Test
    void reloadsFilesAndTellsPluginsByDefault() {
        POSTED.clear();
        int before = PLUGIN_RELOADS.get();

        assertEquals(List.of("Reloaded the whitelist and the ban lists"), run("reload"));

        assertEquals(before, PLUGIN_RELOADS.get());
        assertEquals(List.of(new ServerReloadEvent(false)), POSTED);
    }

    @Test
    void reloadsPluginsOnRequest() {
        POSTED.clear();
        int before = PLUGIN_RELOADS.get();

        List<String> messages = run("reload plugins");

        assertEquals(before + 1, PLUGIN_RELOADS.get());
        assertEquals(List.of("Reloaded " + (before + 1) + (before + 1 == 1 ? " plugin" : " plugins")), messages);
        assertEquals(List.of(new ServerReloadEvent(true)), POSTED);
    }

    @Test
    void rejectsUnknownReloadScopes() {
        assertEquals(List.of("Usage: reload [files|plugins|all]"), run("reload nothing"));
    }

    @Test
    void refusesToRestartWithoutASupervisor() {
        restartable = false;
        int before = RESTARTS.get();

        List<String> messages = run("restart");

        assertTrue(messages.get(0).startsWith("This server was not started by something that restarts it"));
        assertEquals(before, RESTARTS.get());
    }

    @Test
    void restartsWhenSupervised() {
        restartable = true;
        int before = RESTARTS.get();

        assertEquals(List.of("Restarting"), run("restart maintenance"));

        assertEquals(before + 1, RESTARTS.get());
        assertFalse(commands.suggest(commands.console(), "reload ").isEmpty());
    }

    @Test
    void showsVersionDetails() {
        List<String> messages = run("version");

        assertEquals("Tube 1.2.3 (Minecraft 26.2, data version 4999)", messages.get(0));
        assertTrue(messages.get(1).startsWith("Java "));
        assertEquals("0 plugins enabled", messages.get(2));
    }
}

package space.nows.tube.server.command;

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
import space.nows.tube.api.command.Command;
import space.nows.tube.api.command.CommandFailure;
import space.nows.tube.api.command.CommandInfo;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.server.access.PermissionService;
import space.nows.tube.server.player.PlayerService;
import space.nows.tube.server.world.WorldService;

import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class CommandServiceTest {

    @TempDir
    static Path directory;

    private static CommandService commands;
    private static final List<String> RAN = new ArrayList<>();

    @BeforeAll
    static void register() {
        MinecraftServer.init(new Auth.Offline());
        WorldService worlds = new WorldService(directory, "main");
        commands = new CommandService(new PlayerService(worlds, new PermissionService(directory)));
        commands.register("echo", new Command() {
            @Override
            public void execute(CommandSender sender, List<String> args) {
                RAN.add(String.join(" ", args));
            }

            @Override
            public String description() {
                return "Repeats";
            }

            @Override
            public String usage() {
                return "echo <text>";
            }
        }, "say2");
        commands.register("refuse", (sender, args) -> {
            throw new CommandFailure("Not allowed here");
        });
        commands.register("explode", (sender, args) -> {
            throw new IllegalStateException("boom");
        });
    }

    private static List<String> messagesOf(String line, boolean[] found) {
        List<String> messages = new ArrayList<>();
        CommandSender sender = (CommandSender) Proxy.newProxyInstance(CommandSender.class.getClassLoader(), new Class<?>[] {CommandSender.class}, (proxy, method, args) -> {
            switch (method.getName()) {
                case "hasPermission" -> {
                    return true;
                }
                case "name" -> {
                    return "tester";
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
        found[0] = commands.dispatch(sender, line);
        return messages;
    }

    @Test
    void dispatchRunsACommandAndReportsIt() {
        boolean[] found = new boolean[1];

        List<String> messages = messagesOf("echo hello there", found);

        assertTrue(found[0]);
        assertEquals(List.of(), messages);
        assertEquals("hello there", RAN.getLast());
    }

    @Test
    void dispatchReportsUnknownCommands() {
        boolean[] found = new boolean[1];

        List<String> messages = messagesOf("nothing here", found);

        assertFalse(found[0]);
        assertEquals(List.of("Unknown command: nothing. Type \"help\" for help."), messages);
    }

    @Test
    void aFailureIsShownToTheSender() {
        boolean[] found = new boolean[1];

        assertEquals(List.of("Not allowed here"), messagesOf("refuse", found));
        assertTrue(found[0]);
    }

    @Test
    void anUnexpectedErrorDoesNotEscapeTheCommand() {
        boolean[] found = new boolean[1];

        List<String> messages = messagesOf("explode", found);

        assertEquals(List.of("An internal error occurred while trying to perform this command."), messages);
    }

    @Test
    void findsACommandByNameOrAlias() {
        CommandInfo byName = commands.find("echo").orElseThrow();
        CommandInfo byAlias = commands.find("SAY2").orElseThrow();

        assertEquals("echo", byName.name());
        assertEquals(List.of("say2"), byName.aliases());
        assertEquals("Repeats", byName.description());
        assertEquals("echo <text>", byAlias.usage());
        assertTrue(commands.find("missing").isEmpty());
    }
}

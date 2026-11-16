package space.nows.tube.server.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.api.command.Command;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.server.access.PermissionService;
import space.nows.tube.server.player.PlayerService;
import space.nows.tube.server.world.WorldService;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class CommandPermissionTest {

    private static final class Recorder implements CommandSender {

        private final boolean allowed;
        private final List<String> messages = new ArrayList<>();

        Recorder(boolean allowed) {
            this.allowed = allowed;
        }

        @Override
        public String name() {
            return "recorder";
        }

        @Override
        public boolean hasPermission(String node) {
            return allowed;
        }

        @Override
        public void sendMessage(Component message) {
            messages.add(PlainTextComponentSerializer.plainText().serialize(message));
        }
    }

    @TempDir
    Path directory;

    @BeforeAll
    static void initMinestom() {
        MinecraftServer.init(new Auth.Offline());
    }

    @Test
    void protectedCommandsRunOnlyForSendersWithThePermission() {
        WorldService worlds = new WorldService(directory, "main");
        CommandService commands = new CommandService(new PlayerService(worlds, new PermissionService(directory)));
        List<String> ran = new ArrayList<>();
        commands.register("secret", Command.withPermission("tube.secret", (sender, args) -> ran.add(String.join(",", args))));

        Recorder denied = new Recorder(false);
        commands.dispatch(denied, "/secret a b");
        assertEquals(List.of(), ran);
        assertEquals(1, denied.messages.size());
        assertTrue(denied.messages.getFirst().startsWith("I'm sorry, but you do not have permission"));

        commands.dispatch(new Recorder(true), "secret a b");
        assertEquals(List.of("a,b"), ran);
    }

    @Test
    void commandsWithoutAPermissionAreOpenToEveryone() {
        WorldService worlds = new WorldService(directory, "main");
        CommandService commands = new CommandService(new PlayerService(worlds, new PermissionService(directory)));
        List<String> ran = new ArrayList<>();
        commands.register("open", (sender, args) -> ran.add("ran"));

        commands.dispatch(new Recorder(false), "open");

        assertEquals(List.of("ran"), ran);
    }
}

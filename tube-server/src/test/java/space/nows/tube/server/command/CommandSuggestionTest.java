package space.nows.tube.server.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.api.command.Command;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.server.access.PermissionService;
import space.nows.tube.server.player.PlayerService;
import space.nows.tube.server.world.WorldService;

import java.nio.file.Path;
import java.util.List;

class CommandSuggestionTest {

    @TempDir
    static Path directory;

    private static CommandService commands;

    @BeforeAll
    static void register() {
        MinecraftServer.init(new Auth.Offline());
        WorldService worlds = new WorldService(directory, "main");
        commands = new CommandService(new PlayerService(worlds, new PermissionService(directory)));
        commands.register("pick", new Command() {
            @Override
            public void execute(CommandSender sender, List<String> args) {
            }

            @Override
            public List<String> suggest(CommandSender sender, List<String> args) {
                return Suggestions.matching(args.getLast(), List.of("alpha", "beta"));
            }
        });
    }

    private static Suggestion client(String typed) {
        var console = MinecraftServer.getCommandManager().getConsoleSender();
        String input = typed.substring(1);
        if (input.endsWith(" ")) {
            input += '\0';
        }
        return MinecraftServer.getCommandManager().parseCommand(console, input).suggestion(console);
    }

    private static List<String> entries(Suggestion suggestion) {
        return suggestion.getEntries().stream().map(SuggestionEntry::getEntry).toList();
    }

    @Test
    void offersEveryOptionRightAfterTheSpace() {
        Suggestion suggestion = client("/pick ");
        assertEquals(List.of("alpha", "beta"), entries(suggestion));
        assertEquals(6, suggestion.getStart());
        assertEquals(0, suggestion.getLength());
    }

    @Test
    void filtersByWhatIsTyped() {
        Suggestion suggestion = client("/pick al");
        assertEquals(List.of("alpha"), entries(suggestion));
        assertEquals(6, suggestion.getStart());
        assertEquals(2, suggestion.getLength());
    }

    @Test
    void offersTheNextArgumentAfterASpace() {
        Suggestion suggestion = client("/pick alpha ");
        assertEquals(List.of("alpha", "beta"), entries(suggestion));
        assertEquals(12, suggestion.getStart());
        assertEquals(0, suggestion.getLength());
    }
}

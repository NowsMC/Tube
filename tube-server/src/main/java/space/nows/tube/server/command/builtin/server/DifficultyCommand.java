package space.nows.tube.server.command.builtin.server;

import space.nows.tube.api.Server;
import space.nows.tube.api.command.CommandFailure;
import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.argument.Suggestions;
import space.nows.tube.api.command.argument.Typing;
import space.nows.tube.api.world.Difficulty;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.ServerCommand;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

final class DifficultyCommand extends ServerCommand {

    private static final List<String> NAMES = Arrays.stream(Difficulty.values()).map(DifficultyCommand::name).toList();

    private final Server server;

    DifficultyCommand(Server server) {
        super("difficulty", Permissions.command("difficulty"), "difficulty [peaceful|easy|normal|hard]");
        this.server = server;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        if (!input.hasNext()) {
            sender.sendMessage("The difficulty is " + name(server.difficulty()));
            return;
        }
        String text = input.keyword();
        input.end();
        Difficulty difficulty = Arrays.stream(Difficulty.values())
                .filter(value -> name(value).equals(text))
                .findFirst()
                .orElseThrow(() -> new CommandFailure("Unknown difficulty " + text));
        server.setDifficulty(difficulty);
        sender.sendMessage("The difficulty has been set to " + name(difficulty));
    }

    private static String name(Difficulty difficulty) {
        return difficulty.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        return Typing.position(args) == 0 ? Suggestions.matching(Typing.current(args), NAMES) : List.of();
    }
}

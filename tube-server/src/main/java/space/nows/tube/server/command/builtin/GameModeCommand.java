package space.nows.tube.server.command.builtin;

import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.api.entity.GameMode;
import space.nows.tube.api.entity.Player;
import space.nows.tube.server.command.support.CommandInput;
import space.nows.tube.server.command.support.GameModes;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;
import space.nows.tube.server.command.support.Typing;

import java.util.List;

final class GameModeCommand extends ServerCommand {

    private final Targets targets;

    GameModeCommand(Targets targets) {
        super("gamemode", Permissions.command("gamemode"), "gamemode <survival|creative|adventure|spectator> [player]", "gm");
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        GameMode mode = GameModes.parse(input.word());
        List<Player> players = input.hasNext() ? targets.players(sender, input.word()) : List.of(Targets.self(sender));
        input.end();
        for (Player player : players) {
            player.setGameMode(mode);
            sender.sendMessage("Set " + player.name() + "'s game mode to " + GameModes.name(mode));
        }
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        String typed = Typing.current(args);
        return switch (Typing.position(args)) {
            case 0 -> Suggestions.matching(typed, GameModes.NAMES);
            case 1 -> Suggestions.matching(typed, targets.playerOptions());
            default -> List.of();
        };
    }
}

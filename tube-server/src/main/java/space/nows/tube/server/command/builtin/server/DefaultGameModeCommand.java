package space.nows.tube.server.command.builtin.server;

import space.nows.tube.api.Server;
import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.argument.Suggestions;
import space.nows.tube.api.command.argument.Typing;
import space.nows.tube.api.entity.player.GameMode;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.GameModes;
import space.nows.tube.server.command.support.ServerCommand;

import java.util.List;

final class DefaultGameModeCommand extends ServerCommand {

    private final Server server;

    DefaultGameModeCommand(Server server) {
        super("defaultgamemode", Permissions.command("defaultgamemode"), "defaultgamemode <survival|creative|adventure|spectator>");
        this.server = server;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        GameMode mode = GameModes.parse(input.word());
        input.end();
        server.setDefaultGameMode(mode);
        sender.sendMessage("The default game mode is now " + GameModes.name(mode));
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        return Typing.position(args) == 0 ? Suggestions.matching(Typing.current(args), GameModes.NAMES) : List.of();
    }
}

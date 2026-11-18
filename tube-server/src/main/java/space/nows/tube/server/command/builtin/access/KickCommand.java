package space.nows.tube.server.command.builtin.access;

import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.api.entity.Player;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.CommandFailure;
import space.nows.tube.server.command.support.CommandInput;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;
import space.nows.tube.server.command.support.Typing;

import java.util.List;

final class KickCommand extends ServerCommand {

    private final Targets targets;

    KickCommand(Targets targets) {
        super("kick", Permissions.KICK, "kick <player> [reason]");
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        String name = input.word();
        Player target = targets.online(name).orElseThrow(() -> new CommandFailure("Player " + name + " is not online"));
        target.kick(input.hasNext() ? input.rest() : "Kicked by an operator");
        sender.sendMessage("Kicked " + target.name());
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        return Typing.position(args) == 0 ? Suggestions.matching(Typing.current(args), targets.playerNames()) : List.of();
    }
}

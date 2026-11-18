package space.nows.tube.server.command.builtin.access;

import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.entity.Player;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.CommandInput;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;
import space.nows.tube.server.command.support.Typing;

import java.util.List;

final class KickCommand extends ServerCommand {

    private final Targets targets;

    KickCommand(Targets targets) {
        super("kick", Permissions.KICK, "kick <player|selector> [reason]");
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        List<Player> kicked = targets.players(sender, input.word());
        String reason = input.hasNext() ? input.rest() : "Kicked by an operator";
        kicked.forEach(player -> player.kick(reason));
        sender.sendMessage(kicked.size() == 1 ? "Kicked " + kicked.getFirst().name() : "Kicked " + kicked.size() + " players");
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        return Typing.position(args) == 0 ? targets.suggestPlayers(Typing.current(args)) : List.of();
    }
}

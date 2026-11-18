package space.nows.tube.server.command.builtin.info;

import space.nows.tube.api.Server;
import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.entity.Player;
import space.nows.tube.server.command.support.ServerCommand;

import java.util.stream.Collectors;

final class ListCommand extends ServerCommand {

    private final Server server;

    ListCommand(Server server) {
        super("list", null, "list");
        this.server = server;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        String names = server.players().stream().map(Player::name).sorted().collect(Collectors.joining(", "));
        sender.sendMessage("Online (" + server.players().size() + "): " + names);
    }
}

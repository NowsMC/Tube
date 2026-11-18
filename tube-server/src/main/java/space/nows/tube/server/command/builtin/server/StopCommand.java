package space.nows.tube.server.command.builtin.server;

import space.nows.tube.api.Server;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.CommandInput;
import space.nows.tube.server.command.support.ServerCommand;

final class StopCommand extends ServerCommand {

    private final Server server;

    StopCommand(Server server) {
        super("stop", Permissions.ADMIN, "stop");
        this.server = server;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        sender.sendMessage("Stopping");
        server.stop();
    }
}

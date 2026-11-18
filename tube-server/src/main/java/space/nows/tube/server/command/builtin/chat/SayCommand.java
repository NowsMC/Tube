package space.nows.tube.server.command.builtin.chat;

import space.nows.tube.api.Server;
import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.ServerCommand;

final class SayCommand extends ServerCommand {

    private final Server server;

    SayCommand(Server server) {
        super("say", Permissions.command("say"), "say <message>");
        this.server = server;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        server.broadcast("[" + sender.name() + "] " + input.rest());
    }
}

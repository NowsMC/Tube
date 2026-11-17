package space.nows.tube.server.command.builtin;

import space.nows.tube.api.Server;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.server.command.support.CommandInput;
import space.nows.tube.server.command.support.ServerCommand;

final class VersionCommand extends ServerCommand {

    private final Server server;

    VersionCommand(Server server) {
        super("version", null, "version", "ver");
        this.server = server;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        sender.sendMessage(server.name() + " " + server.version() + " (Minecraft " + server.minecraftVersion() + ")");
    }
}

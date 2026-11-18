package space.nows.tube.server.command.builtin.server;

import space.nows.tube.api.Server;
import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.ServerCommand;

final class SaveAllCommand extends ServerCommand {

    private final Server server;

    SaveAllCommand(Server server) {
        super("save-all", Permissions.ADMIN, "save-all");
        this.server = server;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        server.saveAll();
        sender.sendMessage("Saved " + server.worlds().all().size() + " world(s) and " + server.players().size() + " player(s)");
    }
}

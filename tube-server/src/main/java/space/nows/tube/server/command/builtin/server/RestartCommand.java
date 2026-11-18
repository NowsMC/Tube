package space.nows.tube.server.command.builtin.server;

import space.nows.tube.api.Server;
import space.nows.tube.api.command.CommandFailure;
import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.server.Restarter;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.ServerCommand;

final class RestartCommand extends ServerCommand {

    private static final String DEFAULT_REASON = "Server restarting";

    private final Server server;
    private final Restarter restarter;

    RestartCommand(Server server, Restarter restarter) {
        super("restart", Permissions.ADMIN, "restart [reason]");
        this.server = server;
        this.restarter = restarter;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        if (!restarter.available()) {
            throw new CommandFailure("This server was not started by something that restarts it. Start it with `tube run`, or pass -Dtube.restart.exit=<code> and start it again whenever it exits with that code");
        }
        String reason = input.hasNext() ? input.rest() : DEFAULT_REASON;
        sender.sendMessage("Restarting");
        ShutdownNotice.kickEveryone(server, reason);
        restarter.restart();
    }
}

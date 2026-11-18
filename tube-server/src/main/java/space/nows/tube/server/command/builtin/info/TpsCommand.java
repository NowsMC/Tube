package space.nows.tube.server.command.builtin.info;

import space.nows.tube.api.Server;
import space.nows.tube.api.TickReport;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.CommandInput;
import space.nows.tube.server.command.support.ServerCommand;

import java.util.Locale;

final class TpsCommand extends ServerCommand {

    private final Server server;

    TpsCommand(Server server) {
        super("tps", Permissions.command("tps"), "tps");
        this.server = server;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        TickReport ticks = server.ticks();
        sender.sendMessage(String.format(Locale.ROOT, "TPS from last 5s, 1m, 5m: %.1f, %.1f, %.1f (%.1f ms per tick)",
                ticks.lastFiveSeconds(), ticks.lastMinute(), ticks.lastFiveMinutes(), ticks.averageMillis()));
    }
}

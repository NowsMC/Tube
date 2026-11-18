package space.nows.tube.server.command.builtin.info;

import space.nows.tube.api.Server;
import space.nows.tube.server.command.CommandService;
import space.nows.tube.server.command.support.ServerCommand;

import java.util.List;

public final class InfoCommands {

    private InfoCommands() {
    }

    public static List<ServerCommand> create(Server server, CommandService commands) {
        return List.of(
                new HelpCommand(commands),
                new VersionCommand(server),
                new ListCommand(server),
                new PluginsCommand(server),
                new TpsCommand(server));
    }
}

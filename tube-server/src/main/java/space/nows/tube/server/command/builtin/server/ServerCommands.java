package space.nows.tube.server.command.builtin.server;

import space.nows.tube.api.Server;
import space.nows.tube.server.command.support.ServerCommand;

import java.util.List;

public final class ServerCommands {

    private ServerCommands() {
    }

    public static List<ServerCommand> create(Server server) {
        return List.of(
                new SaveAllCommand(server),
                new StopCommand(server),
                new DefaultGameModeCommand(server),
                new DifficultyCommand(server));
    }
}

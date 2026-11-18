package space.nows.tube.server.command.builtin.server;

import space.nows.tube.api.Server;
import space.nows.tube.server.command.support.ServerCommand;

import java.nio.file.Path;
import java.util.List;

public final class ServerCommands {

    private ServerCommands() {
    }

    public static List<ServerCommand> create(Server server, ServerControls controls, Path propertiesFile) {
        return List.of(
                new SaveAllCommand(server),
                new SaveOnCommand(controls.autosave()),
                new SaveOffCommand(controls.autosave()),
                new StopCommand(server),
                new RestartCommand(server, controls.restarter()),
                new ReloadCommand(server, controls),
                new SetIdleTimeoutCommand(controls.idleKicker(), propertiesFile),
                new DefaultGameModeCommand(server),
                new DifficultyCommand(server));
    }
}

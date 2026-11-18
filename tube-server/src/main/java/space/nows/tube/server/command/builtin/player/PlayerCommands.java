package space.nows.tube.server.command.builtin.player;

import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;

import java.util.List;

public final class PlayerCommands {

    private PlayerCommands() {
    }

    public static List<ServerCommand> create(Targets targets) {
        return List.of(
                new GameModeCommand(targets),
                new GiveCommand(targets),
                new ClearCommand(targets),
                new ExperienceCommand(targets),
                new SpawnPointCommand(targets));
    }
}

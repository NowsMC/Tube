package space.nows.tube.server.command.builtin.world;

import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;

import java.util.List;

public final class WorldCommands {

    private WorldCommands() {
    }

    public static List<ServerCommand> create(Targets targets) {
        return List.of(
                new SeedCommand(targets),
                new TimeCommand(targets),
                new SetWorldSpawnCommand(targets),
                new WorldBorderCommand(targets));
    }
}

package space.nows.tube.server.command.builtin.world.block;

import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;

import java.util.List;

public final class BlockCommands {

    private BlockCommands() {
    }

    public static List<ServerCommand> create(Targets targets) {
        return List.of(new SetBlockCommand(targets), new FillCommand(targets), new CloneCommand(targets));
    }
}

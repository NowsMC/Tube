package space.nows.tube.server.command.builtin.display;

import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;

import java.util.List;

public final class DisplayCommands {

    private DisplayCommands() {
    }

    public static List<ServerCommand> create(Targets targets) {
        return List.of(new PlaySoundCommand(targets), new ParticleCommand(targets));
    }
}

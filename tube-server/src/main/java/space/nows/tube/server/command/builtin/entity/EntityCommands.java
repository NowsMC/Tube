package space.nows.tube.server.command.builtin.entity;

import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;

import java.util.List;

public final class EntityCommands {

    private EntityCommands() {
    }

    public static List<ServerCommand> create(Targets targets) {
        return List.of(
                new KillCommand(targets),
                new SummonCommand(targets),
                new TeleportCommand(targets),
                new EffectCommand(targets),
                new TagCommand(targets));
    }
}

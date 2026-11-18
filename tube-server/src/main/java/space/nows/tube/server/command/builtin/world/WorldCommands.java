package space.nows.tube.server.command.builtin.world;

import space.nows.tube.server.command.builtin.world.block.BlockCommands;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;

import java.util.ArrayList;
import java.util.List;

public final class WorldCommands {

    private WorldCommands() {
    }

    public static List<ServerCommand> create(Targets targets) {
        List<ServerCommand> commands = new ArrayList<>(List.of(
                new SeedCommand(targets),
                new TimeCommand(targets),
                new WeatherCommand(targets),
                new GameRuleCommand(targets),
                new SetWorldSpawnCommand(targets),
                new WorldBorderCommand(targets)));
        commands.addAll(BlockCommands.create(targets));
        return commands;
    }
}

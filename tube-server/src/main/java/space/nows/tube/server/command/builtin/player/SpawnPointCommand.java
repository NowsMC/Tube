package space.nows.tube.server.command.builtin.player;

import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Positions;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.api.command.Typing;
import space.nows.tube.api.entity.Player;
import space.nows.tube.api.world.Location;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;

import java.util.List;

final class SpawnPointCommand extends ServerCommand {

    private final Targets targets;

    SpawnPointCommand(Targets targets) {
        super("spawnpoint", Permissions.command("spawnpoint"), "spawnpoint [player] [x y z]");
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        List<Player> players = input.hasNext() ? targets.players(sender, input.word()) : List.of(Targets.self(sender));
        Location chosen = input.hasNext() ? Positions.read(input, targets.origin(sender)) : null;
        input.end();
        for (Player player : players) {
            Location point = chosen != null ? chosen : player.location();
            player.setRespawnPoint(point);
            sender.sendMessage(String.format(java.util.Locale.ROOT, "Set the spawn point of %s to %.0f, %.0f, %.0f",
                    player.name(), Math.floor(point.x()), Math.floor(point.y()), Math.floor(point.z())));
        }
    }

    private List<String> merged(String typed) {
        List<String> options = new java.util.ArrayList<>(targets.suggestPlayers(typed));
        options.addAll(Positions.suggest(List.of(typed), 0));
        return options;
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        if (Typing.position(args) > 0 && Positions.isCoordinate(Typing.word(args, 0))) {
            return Positions.suggest(args, 0);
        }
        return Typing.position(args) == 0 ? merged(Typing.current(args)) : Positions.suggest(args, 1);
    }
}

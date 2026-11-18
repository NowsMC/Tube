package space.nows.tube.server.command.builtin.entity;

import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.api.entity.Player;
import space.nows.tube.api.world.Location;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.CommandInput;
import space.nows.tube.server.command.support.Positions;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;
import space.nows.tube.server.command.support.Typing;

import java.util.List;

final class TeleportCommand extends ServerCommand {

    private final Targets targets;

    TeleportCommand(Targets targets) {
        super("tp", Permissions.command("tp"), "tp <player> | tp <player> <target> | tp [player] <x> <y> <z>", "teleport");
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        switch (input.remaining()) {
            case 1 -> Targets.self(sender).teleport(targets.player(sender, input.word()).location());
            case 2 -> {
                List<Player> movers = targets.players(sender, input.word());
                Location destination = targets.player(sender, input.word()).location();
                movers.forEach(player -> player.teleport(destination));
            }
            case 3 -> {
                Player self = Targets.self(sender);
                self.teleport(Positions.read(input, self.location()));
            }
            case 4 -> {
                List<Player> movers = targets.players(sender, input.word());
                Location destination = Positions.read(input, targets.origin(sender));
                movers.forEach(player -> player.teleport(destination));
            }
            default -> throw input.usage();
        }
        sender.sendMessage("Teleported");
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        String typed = Typing.current(args);
        int position = Typing.position(args);
        if (Positions.isCoordinate(typed)) {
            return List.of();
        }
        if (position > 0 && Positions.isCoordinate(Typing.word(args, 0))) {
            return Positions.suggest(args, 0);
        }
        if (position == 0) {
            List<String> options = new java.util.ArrayList<>(targets.suggestPlayers(typed));
            options.addAll(Positions.suggest(args, 0));
            return options;
        }
        List<String> options = new java.util.ArrayList<>(Positions.suggest(args, 1));
        if (position == 1) {
            options.addAll(targets.suggestPlayers(typed));
        }
        return options;
    }
}

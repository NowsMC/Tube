package space.nows.tube.server.command.builtin.entity;

import space.nows.tube.api.command.CommandFailure;
import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Positions;
import space.nows.tube.api.command.Typing;
import space.nows.tube.api.entity.Entity;
import space.nows.tube.api.world.Location;
import space.nows.tube.api.world.World;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.builtin.entity.spread.SpreadPlanner;
import space.nows.tube.server.command.builtin.entity.spread.SpreadPoint;
import space.nows.tube.server.command.builtin.entity.spread.SurfaceFinder;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;

import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.ThreadLocalRandom;

final class SpreadPlayersCommand extends ServerCommand {

    private final Targets targets;

    SpreadPlayersCommand(Targets targets) {
        super("spreadplayers", Permissions.command("spreadplayers"), "spreadplayers <x> <z> <spreadDistance> <maxRange> <targets>");
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        Location origin = targets.origin(sender);
        double[] center = Positions.readColumn(input, origin);
        double spread = input.decimal();
        double range = input.decimal();
        List<Entity> entities = targets.entities(sender, input.word());
        input.end();
        if (spread < 0) {
            throw new CommandFailure("The spread distance must not be negative");
        }
        if (range < spread + 1) {
            throw new CommandFailure("The max range must be at least " + (spread + 1));
        }
        World world = origin.world();
        SurfaceFinder surface = new SurfaceFinder(world);
        List<SpreadPoint> points = new SpreadPlanner(ThreadLocalRandom.current())
                .plan(center[0], center[1], spread, range, entities.size(), point -> surface.standingHeight(point.x(), point.z()).isPresent())
                .orElseThrow(() -> new CommandFailure("Could not spread " + entities.size() + " entities around " + center[0] + ", " + center[1]
                        + " (too many entities for the space, or no safe ground)"));
        for (int i = 0; i < entities.size(); i++) {
            SpreadPoint point = points.get(i);
            OptionalInt height = surface.standingHeight(point.x(), point.z());
            Entity entity = entities.get(i);
            Location now = entity.location();
            entity.teleport(new Location(world, point.x(), height.isPresent() ? height.getAsInt() : now.y(), point.z(), now.yaw(), now.pitch()));
        }
        sender.sendMessage("Spread " + entities.size() + (entities.size() == 1 ? " entity" : " entities") + " around " + center[0] + ", " + center[1]);
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        int position = Typing.position(args);
        if (position < 2) {
            return Positions.suggest(args, 0);
        }
        return position == 4 ? targets.suggestEntities(Typing.current(args)) : List.of();
    }
}

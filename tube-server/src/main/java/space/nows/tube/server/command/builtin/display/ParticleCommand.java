package space.nows.tube.server.command.builtin.display;

import space.nows.tube.api.command.CommandFailure;
import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Positions;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.api.command.Typing;
import space.nows.tube.api.world.Location;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.RegistryNames;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;

import java.util.List;

final class ParticleCommand extends ServerCommand {

    private static final int MAX_COUNT = 10_000;

    private final Targets targets;

    ParticleCommand(Targets targets) {
        super("particle", Permissions.command("particle"), "particle <name> [x y z] [dx dy dz] [speed] [count]");
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        String name = input.word();
        String key = name.contains(":") ? name : "minecraft:" + name;
        if (!RegistryNames.particles().contains(key)) {
            throw new CommandFailure("Unknown particle " + name);
        }
        Location origin = targets.origin(sender);
        Location at = input.remaining() >= 3 ? Positions.read(input, origin) : origin;
        double dx = 0;
        double dy = 0;
        double dz = 0;
        if (input.remaining() >= 3) {
            dx = input.decimal();
            dy = input.decimal();
            dz = input.decimal();
        }
        double speed = input.hasNext() ? input.decimal() : 0;
        int count = input.integerOr(0);
        input.end();
        if (count < 0 || count > MAX_COUNT) {
            throw new CommandFailure("The count must be between 0 and " + MAX_COUNT);
        }
        origin.world().spawnParticle(key, at.x(), at.y(), at.z(), count, dx, dy, dz, speed);
        sender.sendMessage("Displayed particle " + key);
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        return Typing.position(args) == 0 ? Suggestions.matching(Typing.current(args), RegistryNames.particles()) : Positions.suggest(args, 1);
    }
}

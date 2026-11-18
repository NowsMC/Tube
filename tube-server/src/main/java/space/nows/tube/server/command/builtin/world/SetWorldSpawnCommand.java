package space.nows.tube.server.command.builtin.world;

import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Positions;
import space.nows.tube.api.world.Location;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;

import java.util.List;

final class SetWorldSpawnCommand extends ServerCommand {

    private final Targets targets;

    SetWorldSpawnCommand(Targets targets) {
        super("setworldspawn", Permissions.command("setworldspawn"), "setworldspawn [x y z]");
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        Location origin = targets.origin(sender);
        Positions.BlockPosition block = input.hasNext() ? Positions.readBlock(input, origin)
                : new Positions.BlockPosition((int) Math.floor(origin.x()), (int) Math.floor(origin.y()), (int) Math.floor(origin.z()));
        input.end();
        origin.world().setSpawn(new Location(origin.world(), block.x() + 0.5, block.y(), block.z() + 0.5));
        sender.sendMessage("Set the world spawn point to " + block.x() + ", " + block.y() + ", " + block.z());
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        return Positions.suggest(args, 0);
    }
}

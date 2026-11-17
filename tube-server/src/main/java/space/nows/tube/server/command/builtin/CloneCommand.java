package space.nows.tube.server.command.builtin;

import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.api.world.Block;
import space.nows.tube.api.world.Location;
import space.nows.tube.api.world.World;
import space.nows.tube.server.command.support.BlockBox;
import space.nows.tube.server.command.support.CommandFailure;
import space.nows.tube.server.command.support.CommandInput;
import space.nows.tube.server.command.support.Positions;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;
import space.nows.tube.server.command.support.Typing;

import java.util.List;

final class CloneCommand extends ServerCommand {

    private final Targets targets;

    CloneCommand(Targets targets) {
        super("clone", Permissions.command("clone"), "clone <x1> <y1> <z1> <x2> <y2> <z2> <x> <y> <z> [replace|masked]");
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        Location origin = targets.origin(sender);
        BlockBox source = BlockBox.between(Positions.readBlock(input, origin), Positions.readBlock(input, origin));
        Positions.BlockPosition destination = Positions.readBlock(input, origin);
        String mode = input.hasNext() ? input.keyword() : "replace";
        input.end();
        if (!mode.equals("replace") && !mode.equals("masked")) {
            throw input.usage();
        }
        source.checkEditable();
        Block[] copied = copy(origin.world(), source);
        int changed = paste(origin.world(), source, destination, copied, mode.equals("masked"));
        if (changed == 0) {
            throw new CommandFailure("No blocks were cloned");
        }
        sender.sendMessage("Successfully cloned " + changed + " block(s)");
    }

    private static Block[] copy(World world, BlockBox box) {
        Block[] blocks = new Block[(int) box.volume()];
        int index = 0;
        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int y = box.minY(); y <= box.maxY(); y++) {
                for (int z = box.minZ(); z <= box.maxZ(); z++) {
                    blocks[index++] = world.block(x, y, z);
                }
            }
        }
        return blocks;
    }

    private static int paste(World world, BlockBox box, Positions.BlockPosition to, Block[] blocks, boolean masked) {
        int changed = 0;
        int index = 0;
        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int y = box.minY(); y <= box.maxY(); y++) {
                for (int z = box.minZ(); z <= box.maxZ(); z++) {
                    Block block = blocks[index++];
                    if (masked && block.isAir()) {
                        continue;
                    }
                    world.setBlock(to.x() + x - box.minX(), to.y() + y - box.minY(), to.z() + z - box.minZ(), block);
                    changed++;
                }
            }
        }
        return changed;
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        return Typing.position(args) == 9 ? Suggestions.matching(Typing.current(args), "masked", "replace") : List.of();
    }
}

package space.nows.tube.server.command.builtin;

import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.api.world.Block;
import space.nows.tube.api.world.Location;
import space.nows.tube.api.world.World;
import space.nows.tube.server.command.support.BlockBox;
import space.nows.tube.server.command.support.BlockText;
import space.nows.tube.server.command.support.CommandInput;
import space.nows.tube.server.command.support.Positions;
import space.nows.tube.server.command.support.RegistryNames;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;
import space.nows.tube.server.command.support.Typing;

import java.util.List;

final class FillCommand extends ServerCommand {

    private static final List<String> MODES = List.of("hollow", "keep", "outline", "replace");

    private final Targets targets;

    FillCommand(Targets targets) {
        super("fill", Permissions.command("fill"), "fill <x1> <y1> <z1> <x2> <y2> <z2> <block> [replace [filter]|keep|hollow|outline]");
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        Location origin = targets.origin(sender);
        BlockBox box = BlockBox.between(Positions.readBlock(input, origin), Positions.readBlock(input, origin));
        Block block = BlockText.parse(input.word());
        String mode = input.hasNext() ? input.keyword() : "replace";
        String filter = mode.equals("replace") && input.hasNext() ? BlockText.parse(input.word()).key() : null;
        input.end();
        if (!MODES.contains(mode)) {
            throw input.usage();
        }
        box.checkEditable();
        int changed = fill(origin.world(), box, block, mode, filter);
        sender.sendMessage("Successfully filled " + changed + " block(s)");
    }

    private static int fill(World world, BlockBox box, Block block, String mode, String filter) {
        Block air = Block.AIR;
        int changed = 0;
        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int y = box.minY(); y <= box.maxY(); y++) {
                for (int z = box.minZ(); z <= box.maxZ(); z++) {
                    Block current = world.block(x, y, z);
                    Block next = switch (mode) {
                        case "keep" -> current.isAir() ? block : null;
                        case "outline" -> box.onEdge(x, y, z) ? block : null;
                        case "hollow" -> box.onEdge(x, y, z) ? block : air;
                        default -> filter == null || current.key().equals(filter) ? block : null;
                    };
                    if (next != null && !next.equals(current)) {
                        world.setBlock(x, y, z, next);
                        changed++;
                    }
                }
            }
        }
        return changed;
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        String typed = Typing.current(args);
        return switch (Typing.position(args)) {
            case 6 -> Suggestions.matching(typed, RegistryNames.blocks());
            case 7 -> Suggestions.matching(typed, MODES);
            case 8 -> Typing.word(args, 7).equalsIgnoreCase("replace") ? Suggestions.matching(typed, RegistryNames.blocks()) : List.of();
            default -> Positions.suggest(args, 0);
        };
    }
}

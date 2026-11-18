package space.nows.tube.server.command.builtin.world.block;

import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Positions;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.api.command.Typing;
import space.nows.tube.api.world.Block;
import space.nows.tube.api.world.Location;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.BlockText;
import space.nows.tube.server.command.support.RegistryNames;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;

import java.util.List;

final class SetBlockCommand extends ServerCommand {

    private final Targets targets;

    SetBlockCommand(Targets targets) {
        super("setblock", Permissions.command("setblock"), "setblock <x> <y> <z> <block> [replace|keep]");
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        Location origin = targets.origin(sender);
        Positions.BlockPosition at = Positions.readBlock(input, origin);
        Block block = BlockText.parse(input.word());
        String mode = input.hasNext() ? input.keyword() : "replace";
        input.end();
        if (!mode.equals("replace") && !mode.equals("keep")) {
            throw input.usage();
        }
        if (mode.equals("keep") && !origin.world().block(at.x(), at.y(), at.z()).isAir()) {
            sender.sendMessage("Could not set the block");
            return;
        }
        origin.world().setBlock(at.x(), at.y(), at.z(), block);
        sender.sendMessage("Changed the block at " + at.x() + ", " + at.y() + ", " + at.z());
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        String typed = Typing.current(args);
        return switch (Typing.position(args)) {
            case 3 -> Suggestions.matching(typed, RegistryNames.blocks());
            case 4 -> Suggestions.matching(typed, "keep", "replace");
            default -> Positions.suggest(args, 0);
        };
    }
}

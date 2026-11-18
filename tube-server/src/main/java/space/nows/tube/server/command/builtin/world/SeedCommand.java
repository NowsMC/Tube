package space.nows.tube.server.command.builtin.world;

import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;

final class SeedCommand extends ServerCommand {

    private final Targets targets;

    SeedCommand(Targets targets) {
        super("seed", Permissions.command("seed"), "seed");
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        sender.sendMessage("Seed: [" + targets.origin(sender).world().options().seed() + "]");
    }
}

package space.nows.tube.server.command.builtin.entity;

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

final class SummonCommand extends ServerCommand {

    private final Targets targets;

    SummonCommand(Targets targets) {
        super("summon", Permissions.command("summon"), "summon <entity> [x y z]");
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        String type = input.word();
        Location origin = targets.origin(sender);
        Location where = input.hasNext() ? Positions.read(input, origin) : origin;
        input.end();
        where.world().spawn(type, where);
        sender.sendMessage("Summoned " + type);
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        return Typing.position(args) == 0 ? Suggestions.matching(Typing.current(args), RegistryNames.entities()) : Positions.suggest(args, 1);
    }
}

package space.nows.tube.server.command.builtin.access;

import space.nows.tube.api.command.CommandFailure;
import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.argument.Suggestions;
import space.nows.tube.api.command.argument.Typing;
import space.nows.tube.api.permission.PermissionManager;
import space.nows.tube.server.access.Profiles;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;

import java.util.List;

final class OperatorCommand extends ServerCommand {

    private final boolean grant;
    private final PermissionManager permissions;
    private final Profiles profiles;
    private final Targets targets;

    OperatorCommand(boolean grant, PermissionManager permissions, Profiles profiles, Targets targets) {
        super(grant ? "op" : "deop", Permissions.OP, (grant ? "op" : "deop") + " <player>");
        this.grant = grant;
        this.permissions = permissions;
        this.profiles = profiles;
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        String name = input.word();
        input.end();
        Profiles.Profile profile = profiles.find(name).orElseThrow(() -> new CommandFailure("Unknown player " + name));
        permissions.setOp(profile.uuid(), profile.name(), grant);
        sender.sendMessage(grant ? "Made " + profile.name() + " an operator" : "Removed operator status from " + profile.name());
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        return Typing.position(args) == 0 ? Suggestions.matching(Typing.current(args), targets.playerNames()) : List.of();
    }
}

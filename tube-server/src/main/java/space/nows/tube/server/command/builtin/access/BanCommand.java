package space.nows.tube.server.command.builtin.access;

import space.nows.tube.api.command.CommandFailure;
import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.argument.Typing;
import space.nows.tube.server.access.BanList;
import space.nows.tube.server.access.Profiles;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.Selector;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;

import java.util.List;

final class BanCommand extends ServerCommand {

    private final BanList bans;
    private final Profiles profiles;
    private final Targets targets;

    BanCommand(BanList bans, Profiles profiles, Targets targets) {
        super("ban", Permissions.BAN, "ban <player|selector> [reason]");
        this.bans = bans;
        this.profiles = profiles;
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        String name = input.word();
        List<Profiles.Profile> banned = Selector.is(name) ? online(sender, name)
                : List.of(profiles.find(name).orElseThrow(() -> new CommandFailure("Unknown player " + name)));
        String reason = input.hasNext() ? input.rest() : BanList.DEFAULT_REASON;
        for (Profiles.Profile profile : banned) {
            bans.banPlayer(profile.uuid(), profile.name(), reason, sender.name());
            targets.online(profile.name()).ifPresent(player -> player.kick("You are banned from this server.\nReason: " + reason));
        }
        sender.sendMessage(banned.size() == 1 ? "Banned " + banned.getFirst().name() + ": " + reason : "Banned " + banned.size() + " players: " + reason);
    }

    private List<Profiles.Profile> online(CommandSender sender, String selector) {
        return targets.players(sender, selector).stream().map(player -> new Profiles.Profile(player.uuid(), player.name())).toList();
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        return Typing.position(args) == 0 ? targets.suggestPlayers(Typing.current(args)) : List.of();
    }
}

package space.nows.tube.server.command.builtin.access;

import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.server.access.BanList;
import space.nows.tube.server.access.Profiles;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.CommandFailure;
import space.nows.tube.server.command.support.CommandInput;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;
import space.nows.tube.server.command.support.Typing;

import java.util.List;

final class BanCommand extends ServerCommand {

    private final BanList bans;
    private final Profiles profiles;
    private final Targets targets;

    BanCommand(BanList bans, Profiles profiles, Targets targets) {
        super("ban", Permissions.BAN, "ban <player> [reason]");
        this.bans = bans;
        this.profiles = profiles;
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        String name = input.word();
        Profiles.Profile profile = profiles.find(name).orElseThrow(() -> new CommandFailure("Unknown player " + name));
        String reason = input.hasNext() ? input.rest() : BanList.DEFAULT_REASON;
        bans.banPlayer(profile.uuid(), profile.name(), reason, sender.name());
        targets.online(profile.name()).ifPresent(player -> player.kick("You are banned from this server.\nReason: " + reason));
        sender.sendMessage("Banned " + profile.name() + ": " + reason);
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        return Typing.position(args) == 0 ? Suggestions.matching(Typing.current(args), targets.playerNames()) : List.of();
    }
}

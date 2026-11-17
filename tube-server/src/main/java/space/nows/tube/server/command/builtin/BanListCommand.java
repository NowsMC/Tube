package space.nows.tube.server.command.builtin;

import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.server.access.BanList;
import space.nows.tube.server.command.support.CommandInput;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Typing;

import java.util.List;

final class BanListCommand extends ServerCommand {

    private final BanList bans;

    BanListCommand(BanList bans) {
        super("banlist", Permissions.BAN, "banlist [ips|players]");
        this.bans = bans;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        boolean ips = input.wordOr("players").equalsIgnoreCase("ips");
        List<BanList.Ban> entries = ips ? bans.ips() : bans.players();
        sender.sendMessage("There are " + entries.size() + " banned " + (ips ? "IPs" : "players"));
        entries.forEach(ban -> sender.sendMessage(ban.name() + " was banned by " + ban.source() + ": " + ban.reason()));
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        return Typing.position(args) == 0 ? Suggestions.matching(Typing.current(args), "ips", "players") : List.of();
    }
}

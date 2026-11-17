package space.nows.tube.server.command.builtin;

import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.server.access.BanList;
import space.nows.tube.server.command.support.CommandInput;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Typing;

import java.util.List;

final class PardonIpCommand extends ServerCommand {

    private final BanList bans;

    PardonIpCommand(BanList bans) {
        super("pardon-ip", Permissions.BAN, "pardon-ip <address>");
        this.bans = bans;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        String address = input.word();
        input.end();
        sender.sendMessage(bans.pardonIp(address) ? "Unbanned IP " + address : address + " is not banned");
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        if (Typing.position(args) != 0) {
            return List.of();
        }
        return Suggestions.matching(Typing.current(args), bans.ips().stream().map(BanList.Ban::key).toList());
    }
}

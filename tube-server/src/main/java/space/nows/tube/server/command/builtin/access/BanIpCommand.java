package space.nows.tube.server.command.builtin.access;

import space.nows.tube.api.Server;
import space.nows.tube.api.command.CommandFailure;
import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.api.command.Typing;
import space.nows.tube.api.entity.Player;
import space.nows.tube.server.access.AccessControl;
import space.nows.tube.server.access.BanList;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;

import java.util.List;
import java.util.regex.Pattern;

final class BanIpCommand extends ServerCommand {

    private static final Pattern IP_ADDRESS = Pattern.compile("[0-9a-fA-F:.]+");

    private final Server server;
    private final BanList bans;
    private final Targets targets;

    BanIpCommand(Server server, BanList bans, Targets targets) {
        super("ban-ip", Permissions.BAN, "ban-ip <address|player> [reason]");
        this.server = server;
        this.bans = bans;
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        String address = resolve(input.word());
        String reason = input.hasNext() ? input.rest() : BanList.DEFAULT_REASON;
        bans.banIp(address, reason, sender.name());
        server.players().stream()
                .filter(player -> player.address().map(AccessControl::host).filter(address::equals).isPresent())
                .forEach(player -> player.kick("Your IP address is banned from this server.\nReason: " + reason));
        sender.sendMessage("Banned IP " + address + ": " + reason);
    }

    private String resolve(String text) {
        if (IP_ADDRESS.matcher(text).matches()) {
            return text;
        }
        return targets.online(text)
                .flatMap(Player::address)
                .map(AccessControl::host)
                .orElseThrow(() -> new CommandFailure("Invalid address or the player is not online"));
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        return Typing.position(args) == 0 ? Suggestions.matching(Typing.current(args), targets.playerNames()) : List.of();
    }
}

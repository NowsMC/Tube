package space.nows.tube.server.command.builtin.access;

import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.argument.Suggestions;
import space.nows.tube.api.command.argument.Typing;
import space.nows.tube.server.access.BanList;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.ServerCommand;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

final class PardonCommand extends ServerCommand {

    private final BanList bans;

    PardonCommand(BanList bans) {
        super("pardon", Permissions.BAN, "pardon <player>");
        this.bans = bans;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        String name = input.word();
        input.end();
        Optional<UUID> uuid = bans.players().stream()
                .filter(ban -> ban.name().equalsIgnoreCase(name))
                .map(ban -> UUID.fromString(ban.key()))
                .findFirst();
        if (uuid.isEmpty() || !bans.pardonPlayer(uuid.get())) {
            sender.sendMessage(name + " is not banned");
            return;
        }
        sender.sendMessage("Unbanned " + name);
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        if (Typing.position(args) != 0) {
            return List.of();
        }
        return Suggestions.matching(Typing.current(args), bans.players().stream().map(BanList.Ban::name).toList());
    }
}

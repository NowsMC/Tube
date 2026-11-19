package space.nows.tube.server.command.builtin.access;

import space.nows.tube.api.Server;
import space.nows.tube.api.permission.PermissionManager;
import space.nows.tube.server.access.BanList;
import space.nows.tube.server.access.Profiles;
import space.nows.tube.server.access.Whitelist;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;

import java.nio.file.Path;
import java.util.List;

public final class AccessCommands {

    private AccessCommands() {
    }

    public static List<ServerCommand> create(Server server, Targets targets, Profiles profiles, BanList bans, Whitelist whitelist,
                                             PermissionManager permissions, Path propertiesFile, boolean enforceWhitelist) {
        return List.of(
                new OperatorCommand(true, permissions, profiles, targets),
                new OperatorCommand(false, permissions, profiles, targets),
                new KickCommand(targets),
                new BanCommand(bans, profiles, targets),
                new PardonCommand(bans),
                new BanIpCommand(server, bans, targets),
                new PardonIpCommand(bans),
                new BanListCommand(bans),
                new WhitelistCommand(server, whitelist, profiles, targets, propertiesFile, enforceWhitelist));
    }
}

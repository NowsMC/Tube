package space.nows.tube.server;

import net.kyori.adventure.text.Component;

import java.util.Optional;
import java.util.UUID;

final class AccessControl {

    private final Whitelist whitelist;
    private final BanList bans;

    AccessControl(Whitelist whitelist, BanList bans) {
        this.whitelist = whitelist;
        this.bans = bans;
    }

    Optional<Component> deny(UUID uuid, String ip) {
        Optional<BanList.Ban> playerBan = bans.player(uuid);
        if (playerBan.isPresent()) {
            return Optional.of(Component.text("You are banned from this server.\nReason: " + playerBan.get().reason()));
        }
        if (ip != null) {
            Optional<BanList.Ban> ipBan = bans.ip(ip);
            if (ipBan.isPresent()) {
                return Optional.of(Component.text("Your IP address is banned from this server.\nReason: " + ipBan.get().reason()));
            }
        }
        if (whitelist.enabled() && !whitelist.contains(uuid)) {
            return Optional.of(Component.text("You are not white-listed on this server!"));
        }
        return Optional.empty();
    }
}

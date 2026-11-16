package space.nows.tube.server.access;

import net.kyori.adventure.text.Component;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;

public final class AccessControl {

    private final Whitelist whitelist;
    private final BanList bans;

    public AccessControl(Whitelist whitelist, BanList bans) {
        this.whitelist = whitelist;
        this.bans = bans;
    }

    public Optional<Component> deny(UUID uuid, String ip) {
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

    public static String host(InetSocketAddress address) {
        return address.getAddress() == null ? address.getHostString() : address.getAddress().getHostAddress();
    }
}

package space.nows.tube.api.event;

import net.kyori.adventure.text.Component;

import java.net.SocketAddress;
import java.util.Objects;

public final class ServerListPingEvent implements Cancellable {

    private final SocketAddress remoteAddress;
    private final int protocolVersion;
    private Component motd;
    private int online;
    private int maxPlayers;
    private boolean hidePlayers;
    private boolean cancelled;

    public ServerListPingEvent(SocketAddress remoteAddress, int protocolVersion, Component motd, int online, int maxPlayers, boolean hidePlayers) {
        this.remoteAddress = remoteAddress;
        this.protocolVersion = protocolVersion;
        this.motd = Objects.requireNonNull(motd, "motd");
        this.online = online;
        this.maxPlayers = maxPlayers;
        this.hidePlayers = hidePlayers;
    }

    public SocketAddress remoteAddress() {
        return remoteAddress;
    }

    public int protocolVersion() {
        return protocolVersion;
    }

    public Component motd() {
        return motd;
    }

    public void setMotd(Component motd) {
        this.motd = Objects.requireNonNull(motd, "motd");
    }

    public int online() {
        return online;
    }

    public void setOnline(int online) {
        this.online = online;
    }

    public int maxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public boolean hidePlayers() {
        return hidePlayers;
    }

    public void setHidePlayers(boolean hidePlayers) {
        this.hidePlayers = hidePlayers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}

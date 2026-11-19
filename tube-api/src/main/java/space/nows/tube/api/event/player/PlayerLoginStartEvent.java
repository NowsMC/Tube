package space.nows.tube.api.event.player;

import net.kyori.adventure.text.Component;
import space.nows.tube.api.entity.player.LoginMode;
import space.nows.tube.api.event.Event;

import java.net.SocketAddress;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class PlayerLoginStartEvent implements Event {

    private final String name;
    private final UUID claimedUuid;
    private final SocketAddress address;
    private LoginMode mode;
    private Component denyReason;

    public PlayerLoginStartEvent(String name, UUID claimedUuid, SocketAddress address, LoginMode mode) {
        this.name = name;
        this.claimedUuid = claimedUuid;
        this.address = address;
        this.mode = Objects.requireNonNull(mode, "mode");
    }

    public String name() {
        return name;
    }

    public UUID claimedUuid() {
        return claimedUuid;
    }

    public SocketAddress address() {
        return address;
    }

    public LoginMode mode() {
        return mode;
    }

    public void setMode(LoginMode mode) {
        this.mode = Objects.requireNonNull(mode, "mode");
    }

    public Optional<Component> denyReason() {
        return Optional.ofNullable(denyReason);
    }

    public void deny(Component reason) {
        this.denyReason = Objects.requireNonNull(reason, "reason");
    }

    public void allow() {
        this.denyReason = null;
    }
}

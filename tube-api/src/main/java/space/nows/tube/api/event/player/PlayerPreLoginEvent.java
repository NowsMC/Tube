package space.nows.tube.api.event.player;

import net.kyori.adventure.text.Component;
import space.nows.tube.api.entity.player.LoginMode;
import space.nows.tube.api.event.Event;

import java.net.SocketAddress;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class PlayerPreLoginEvent implements Event {

    private final String name;
    private final UUID uuid;
    private final SocketAddress address;
    private final LoginMode mode;
    private Component denyReason;

    public PlayerPreLoginEvent(String name, UUID uuid, SocketAddress address, LoginMode mode) {
        this.name = name;
        this.uuid = uuid;
        this.address = address;
        this.mode = mode;
    }

    public String name() {
        return name;
    }

    public UUID uuid() {
        return uuid;
    }

    public SocketAddress address() {
        return address;
    }

    public LoginMode mode() {
        return mode;
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

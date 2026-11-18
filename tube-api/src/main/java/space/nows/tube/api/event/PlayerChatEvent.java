package space.nows.tube.api.event;

import net.kyori.adventure.text.Component;
import space.nows.tube.api.entity.Player;

import java.util.Objects;
import java.util.Optional;

public final class PlayerChatEvent implements Cancellable {

    private final Player player;
    private String message;
    private Component formatted;
    private boolean cancelled;

    public PlayerChatEvent(Player player, String message) {
        this.player = player;
        this.message = Objects.requireNonNull(message, "message");
    }

    public Player player() {
        return player;
    }

    public String message() {
        return message;
    }

    public void setMessage(String message) {
        this.message = Objects.requireNonNull(message, "message");
    }

    public Optional<Component> formatted() {
        return Optional.ofNullable(formatted);
    }

    public void setFormatted(Component formatted) {
        this.formatted = formatted;
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

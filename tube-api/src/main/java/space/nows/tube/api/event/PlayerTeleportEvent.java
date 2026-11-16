package space.nows.tube.api.event;

import space.nows.tube.api.entity.Player;
import space.nows.tube.api.world.Location;

import java.util.Objects;

public final class PlayerTeleportEvent implements Cancellable {

    private final Player player;
    private final Location from;
    private Location to;
    private boolean cancelled;

    public PlayerTeleportEvent(Player player, Location from, Location to) {
        this.player = player;
        this.from = from;
        this.to = Objects.requireNonNull(to, "to");
    }

    public Player player() {
        return player;
    }

    public Location from() {
        return from;
    }

    public Location to() {
        return to;
    }

    public void setTo(Location to) {
        this.to = Objects.requireNonNull(to, "to");
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

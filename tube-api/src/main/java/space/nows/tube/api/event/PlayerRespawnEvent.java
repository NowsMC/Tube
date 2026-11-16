package space.nows.tube.api.event;

import space.nows.tube.api.entity.Player;
import space.nows.tube.api.world.Location;

import java.util.Objects;

public final class PlayerRespawnEvent implements Event {

    private final Player player;
    private Location location;

    public PlayerRespawnEvent(Player player, Location location) {
        this.player = player;
        this.location = Objects.requireNonNull(location, "location");
    }

    public Player player() {
        return player;
    }

    public Location location() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = Objects.requireNonNull(location, "location");
    }
}

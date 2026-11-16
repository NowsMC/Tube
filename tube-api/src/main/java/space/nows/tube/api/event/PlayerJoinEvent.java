package space.nows.tube.api.event;

import space.nows.tube.api.Player;

public final class PlayerJoinEvent implements Event {

    private final Player player;

    public PlayerJoinEvent(Player player) {
        this.player = player;
    }

    public Player player() {
        return player;
    }
}

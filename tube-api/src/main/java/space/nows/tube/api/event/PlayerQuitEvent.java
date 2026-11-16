package space.nows.tube.api.event;

import space.nows.tube.api.Player;

public final class PlayerQuitEvent implements Event {

    private final Player player;

    public PlayerQuitEvent(Player player) {
        this.player = player;
    }

    public Player player() {
        return player;
    }
}

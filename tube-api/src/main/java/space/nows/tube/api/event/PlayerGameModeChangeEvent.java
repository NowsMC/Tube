package space.nows.tube.api.event;

import space.nows.tube.api.entity.GameMode;
import space.nows.tube.api.entity.Player;

public final class PlayerGameModeChangeEvent implements Cancellable {

    private final Player player;
    private final GameMode newGameMode;
    private boolean cancelled;

    public PlayerGameModeChangeEvent(Player player, GameMode newGameMode) {
        this.player = player;
        this.newGameMode = newGameMode;
    }

    public Player player() {
        return player;
    }

    public GameMode newGameMode() {
        return newGameMode;
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

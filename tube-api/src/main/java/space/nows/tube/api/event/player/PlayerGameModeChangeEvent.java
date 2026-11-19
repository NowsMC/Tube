package space.nows.tube.api.event.player;

import space.nows.tube.api.entity.player.GameMode;
import space.nows.tube.api.entity.player.Player;
import space.nows.tube.api.event.Cancellable;

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

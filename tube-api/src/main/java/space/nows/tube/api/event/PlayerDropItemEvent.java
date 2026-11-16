package space.nows.tube.api.event;

import space.nows.tube.api.entity.ItemEntity;
import space.nows.tube.api.entity.Player;

public final class PlayerDropItemEvent implements Cancellable {

    private final Player player;
    private final ItemEntity item;
    private boolean cancelled;

    public PlayerDropItemEvent(Player player, ItemEntity item) {
        this.player = player;
        this.item = item;
    }

    public Player player() {
        return player;
    }

    public ItemEntity item() {
        return item;
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

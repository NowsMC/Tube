package space.nows.tube.api.event.inventory;

import space.nows.tube.api.entity.player.Player;
import space.nows.tube.api.event.Cancellable;
import space.nows.tube.api.inventory.Inventory;

public final class InventoryOpenEvent implements Cancellable {

    private final Player player;
    private final Inventory inventory;
    private boolean cancelled;

    public InventoryOpenEvent(Player player, Inventory inventory) {
        this.player = player;
        this.inventory = inventory;
    }

    public Player player() {
        return player;
    }

    public Inventory inventory() {
        return inventory;
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

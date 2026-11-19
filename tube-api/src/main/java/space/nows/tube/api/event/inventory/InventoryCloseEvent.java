package space.nows.tube.api.event.inventory;

import space.nows.tube.api.entity.player.Player;
import space.nows.tube.api.event.Event;
import space.nows.tube.api.inventory.Inventory;

public final class InventoryCloseEvent implements Event {

    private final Player player;
    private final Inventory inventory;

    public InventoryCloseEvent(Player player, Inventory inventory) {
        this.player = player;
        this.inventory = inventory;
    }

    public Player player() {
        return player;
    }

    public Inventory inventory() {
        return inventory;
    }
}

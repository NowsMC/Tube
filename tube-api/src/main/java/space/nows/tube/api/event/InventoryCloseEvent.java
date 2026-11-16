package space.nows.tube.api.event;

import space.nows.tube.api.Inventory;
import space.nows.tube.api.Player;

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

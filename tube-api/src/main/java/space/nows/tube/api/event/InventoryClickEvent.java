package space.nows.tube.api.event;

import space.nows.tube.api.entity.Player;
import space.nows.tube.api.inventory.Inventory;
import space.nows.tube.api.inventory.ItemStack;

public final class InventoryClickEvent implements Cancellable {

    private final Player player;
    private final Inventory inventory;
    private final int slot;
    private final ItemStack clicked;
    private boolean cancelled;

    public InventoryClickEvent(Player player, Inventory inventory, int slot, ItemStack clicked) {
        this.player = player;
        this.inventory = inventory;
        this.slot = slot;
        this.clicked = clicked;
    }

    public Player player() {
        return player;
    }

    public Inventory inventory() {
        return inventory;
    }

    public int slot() {
        return slot;
    }

    public ItemStack clicked() {
        return clicked;
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

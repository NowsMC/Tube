package space.nows.tube.api.event.inventory;

import space.nows.tube.api.entity.player.Player;
import space.nows.tube.api.event.Cancellable;
import space.nows.tube.api.inventory.ClickType;
import space.nows.tube.api.inventory.Inventory;
import space.nows.tube.api.inventory.ItemStack;

public final class InventoryClickEvent implements Cancellable {

    private final Player player;
    private final Inventory inventory;
    private final int slot;
    private final ItemStack clicked;
    private final ClickType click;
    private final int hotbarButton;
    private boolean cancelled;

    public InventoryClickEvent(Player player, Inventory inventory, int slot, ItemStack clicked, ClickType click, int hotbarButton) {
        this.player = player;
        this.inventory = inventory;
        this.slot = slot;
        this.clicked = clicked;
        this.click = click;
        this.hotbarButton = hotbarButton;
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

    public ClickType click() {
        return click;
    }

    public int hotbarButton() {
        return hotbarButton;
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

package space.nows.tube.api.event.inventory;

import space.nows.tube.api.entity.player.Player;
import space.nows.tube.api.event.Cancellable;
import space.nows.tube.api.inventory.Inventory;

import java.util.List;

public final class InventoryDragEvent implements Cancellable {

    private final Player player;
    private final Inventory inventory;
    private final List<Integer> slots;
    private final boolean rightClick;
    private boolean cancelled;

    public InventoryDragEvent(Player player, Inventory inventory, List<Integer> slots, boolean rightClick) {
        this.player = player;
        this.inventory = inventory;
        this.slots = List.copyOf(slots);
        this.rightClick = rightClick;
    }

    public Player player() {
        return player;
    }

    public Inventory inventory() {
        return inventory;
    }

    public List<Integer> slots() {
        return slots;
    }

    public boolean isRightClick() {
        return rightClick;
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

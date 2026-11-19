package space.nows.tube.api.event.inventory;

import space.nows.tube.api.entity.player.Player;
import space.nows.tube.api.event.Cancellable;
import space.nows.tube.api.inventory.ItemStack;

public final class CraftItemEvent implements Cancellable {

    private final Player player;
    private final ItemStack result;
    private boolean cancelled;

    public CraftItemEvent(Player player, ItemStack result) {
        this.player = player;
        this.result = result;
    }

    public Player player() {
        return player;
    }

    public ItemStack result() {
        return result;
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

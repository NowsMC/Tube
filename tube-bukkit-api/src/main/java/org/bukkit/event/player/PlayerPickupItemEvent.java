package org.bukkit.event.player;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

@Deprecated
public class PlayerPickupItemEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Item item;
    private final int remaining;
    private boolean cancelled;

    public PlayerPickupItemEvent(Player player, Item item, int remaining) {
        super(player);
        this.item = item;
        this.remaining = remaining;
    }

    public Item getItem() {
        return item;
    }

    public int getRemaining() {
        return remaining;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

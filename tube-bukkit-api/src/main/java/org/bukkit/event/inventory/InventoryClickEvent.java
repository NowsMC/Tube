package org.bukkit.event.inventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryClickEvent extends InventoryEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;
    private final HumanEntity whoClicked;
    private final int slot;
    private final ItemStack current;

    public InventoryClickEvent(HumanEntity whoClicked, Inventory clicked, int slot, ItemStack current) {
        super(clicked);
        this.whoClicked = whoClicked;
        this.slot = slot;
        this.current = current;
    }

    public HumanEntity getWhoClicked() {
        return whoClicked;
    }

    public Inventory getClickedInventory() {
        return getInventory();
    }

    public int getSlot() {
        return slot;
    }

    public int getRawSlot() {
        return slot;
    }

    public ItemStack getCurrentItem() {
        return current;
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

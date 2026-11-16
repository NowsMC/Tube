package org.bukkit.event.inventory;

public class InventoryPickupItemEvent extends org.bukkit.event.Event implements org.bukkit.event.Cancellable {
    private boolean cancelled;
    private org.bukkit.inventory.Inventory inventory;
    private org.bukkit.entity.Item item;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public InventoryPickupItemEvent(org.bukkit.inventory.Inventory p0, org.bukkit.entity.Item p1) {
        super();
        this.inventory = p0;
        this.item = p1;
    }

    public org.bukkit.inventory.Inventory getInventory() {
        return inventory;
    }

    public org.bukkit.entity.Item getItem() {
        return item;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean p0) {
        this.cancelled = p0;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}

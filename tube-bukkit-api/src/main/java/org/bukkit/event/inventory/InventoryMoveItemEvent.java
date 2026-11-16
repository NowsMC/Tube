package org.bukkit.event.inventory;

public class InventoryMoveItemEvent extends org.bukkit.event.Event implements org.bukkit.event.Cancellable {
    private boolean cancelled;
    private org.bukkit.inventory.Inventory sourceInventory;
    private org.bukkit.inventory.Inventory destinationInventory;
    private org.bukkit.inventory.ItemStack itemStack;
    private boolean didSourceInitiate;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public InventoryMoveItemEvent(org.bukkit.inventory.Inventory p0, org.bukkit.inventory.ItemStack p1, org.bukkit.inventory.Inventory p2, boolean p3) {
        super();
        this.sourceInventory = p0;
        this.itemStack = p1;
        this.destinationInventory = p2;
        this.didSourceInitiate = p3;
    }

    public org.bukkit.inventory.Inventory getSource() {
        return sourceInventory;
    }

    public org.bukkit.inventory.ItemStack getItem() {
        throw new UnsupportedOperationException("InventoryMoveItemEvent#getItem is not supported by Tube yet");
    }

    public void setItem(org.bukkit.inventory.ItemStack p0) {
        throw new UnsupportedOperationException("InventoryMoveItemEvent#setItem is not supported by Tube yet");
    }

    public org.bukkit.inventory.Inventory getDestination() {
        return destinationInventory;
    }

    public org.bukkit.inventory.Inventory getInitiator() {
        throw new UnsupportedOperationException("InventoryMoveItemEvent#getInitiator is not supported by Tube yet");
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

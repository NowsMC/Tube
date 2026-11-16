package org.bukkit.event.block;

public class BlockDispenseEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    private boolean cancelled;
    private org.bukkit.inventory.ItemStack item;
    private org.bukkit.util.Vector velocity;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public BlockDispenseEvent(org.bukkit.block.Block p0, org.bukkit.inventory.ItemStack p1, org.bukkit.util.Vector p2) {
        super(p0);
        this.cancelled = false;
        this.item = p1;
        this.velocity = p2;
    }

    public org.bukkit.inventory.ItemStack getItem() {
        throw new UnsupportedOperationException("BlockDispenseEvent#getItem is not supported by Tube yet");
    }

    public void setItem(org.bukkit.inventory.ItemStack p0) {
        this.item = p0;
    }

    public org.bukkit.util.Vector getVelocity() {
        throw new UnsupportedOperationException("BlockDispenseEvent#getVelocity is not supported by Tube yet");
    }

    public void setVelocity(org.bukkit.util.Vector p0) {
        this.velocity = p0;
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

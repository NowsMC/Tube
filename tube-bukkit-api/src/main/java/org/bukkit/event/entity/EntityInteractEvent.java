package org.bukkit.event.entity;

public class EntityInteractEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    protected org.bukkit.block.Block block;
    private boolean cancelled;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public EntityInteractEvent(org.bukkit.entity.Entity p0, org.bukkit.block.Block p1) {
        super(p0);
        this.block = p1;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean p0) {
        this.cancelled = p0;
    }

    public org.bukkit.block.Block getBlock() {
        return block;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}

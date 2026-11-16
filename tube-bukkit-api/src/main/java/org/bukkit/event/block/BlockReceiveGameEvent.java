package org.bukkit.event.block;

public class BlockReceiveGameEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    private org.bukkit.entity.Entity entity;
    private boolean cancelled;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public BlockReceiveGameEvent(org.bukkit.block.Block p0, org.bukkit.entity.Entity p1) {
        super(p0);
        this.entity = p1;
    }

    public org.bukkit.entity.Entity getEntity() {
        return entity;
    }

    public void setCancelled(boolean p0) {
        this.cancelled = p0;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}

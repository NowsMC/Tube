package org.bukkit.event.entity;

public class EntityChangeBlockEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    private org.bukkit.block.Block block;
    private boolean cancel;
    private org.bukkit.block.data.BlockData to;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public EntityChangeBlockEvent(org.bukkit.entity.Entity p0, org.bukkit.block.Block p1, org.bukkit.block.data.BlockData p2) {
        super(p0);
        this.block = p1;
        this.cancel = false;
        this.to = p2;
    }

    public org.bukkit.block.Block getBlock() {
        return block;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean p0) {
        this.cancel = p0;
    }

    public org.bukkit.Material getTo() {
        throw new UnsupportedOperationException("EntityChangeBlockEvent#getTo is not supported by Tube yet");
    }

    public org.bukkit.block.data.BlockData getBlockData() {
        return to;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}

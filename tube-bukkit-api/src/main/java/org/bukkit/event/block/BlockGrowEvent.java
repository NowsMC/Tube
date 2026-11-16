package org.bukkit.event.block;

public class BlockGrowEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    private org.bukkit.block.BlockState newState;
    private boolean cancelled;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public BlockGrowEvent(org.bukkit.block.Block p0, org.bukkit.block.BlockState p1) {
        super(p0);
        this.cancelled = false;
        this.newState = p1;
    }

    public org.bukkit.block.BlockState getNewState() {
        return newState;
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

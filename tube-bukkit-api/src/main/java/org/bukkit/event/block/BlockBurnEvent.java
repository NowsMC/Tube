package org.bukkit.event.block;

public class BlockBurnEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    private boolean cancelled;
    private org.bukkit.block.Block ignitingBlock;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public BlockBurnEvent(org.bukkit.block.Block p0) {
        this(p0, (org.bukkit.block.Block) null);
    }

    public BlockBurnEvent(org.bukkit.block.Block p0, org.bukkit.block.Block p1) {
        super(p0);
        this.ignitingBlock = p1;
    }

    public org.bukkit.block.Block getIgnitingBlock() {
        return ignitingBlock;
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

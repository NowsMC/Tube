package org.bukkit.event.block;

public class BlockPhysicsEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    private org.bukkit.block.data.BlockData changed;
    private org.bukkit.block.Block sourceBlock;
    private boolean cancel;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public BlockPhysicsEvent(org.bukkit.block.Block p0, org.bukkit.block.data.BlockData p1) {
        this(p0, p1, p0);
    }

    public BlockPhysicsEvent(org.bukkit.block.Block p0, org.bukkit.block.data.BlockData p1, org.bukkit.block.Block p2) {
        super(p0);
        this.cancel = false;
        this.changed = p1;
        this.sourceBlock = p2;
    }

    public org.bukkit.block.Block getSourceBlock() {
        return sourceBlock;
    }

    public org.bukkit.Material getChangedType() {
        throw new UnsupportedOperationException("BlockPhysicsEvent#getChangedType is not supported by Tube yet");
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean p0) {
        this.cancel = p0;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}

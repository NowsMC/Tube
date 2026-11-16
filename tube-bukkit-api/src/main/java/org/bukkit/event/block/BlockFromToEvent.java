package org.bukkit.event.block;

public class BlockFromToEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    protected org.bukkit.block.Block to;
    protected org.bukkit.block.BlockFace face;
    protected boolean cancel;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public BlockFromToEvent(org.bukkit.block.Block p0, org.bukkit.block.BlockFace p1) {
        super(p0);
        this.face = p1;
        this.cancel = false;
    }

    public BlockFromToEvent(org.bukkit.block.Block p0, org.bukkit.block.Block p1) {
        super(p0);
        this.to = p1;
        this.face = org.bukkit.block.BlockFace.SELF;
        this.cancel = false;
    }

    public org.bukkit.block.BlockFace getFace() {
        return face;
    }

    public org.bukkit.block.Block getToBlock() {
        throw new UnsupportedOperationException("BlockFromToEvent#getToBlock is not supported by Tube yet");
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

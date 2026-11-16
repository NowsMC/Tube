package org.bukkit.event.block;

public class BlockRedstoneEvent extends org.bukkit.event.block.BlockEvent {
    private int oldCurrent;
    private int newCurrent;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public BlockRedstoneEvent(org.bukkit.block.Block p0, int p1, int p2) {
        super(p0);
        this.oldCurrent = p1;
        this.newCurrent = p2;
    }

    public int getOldCurrent() {
        return oldCurrent;
    }

    public int getNewCurrent() {
        return newCurrent;
    }

    public void setNewCurrent(int p0) {
        this.newCurrent = p0;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}

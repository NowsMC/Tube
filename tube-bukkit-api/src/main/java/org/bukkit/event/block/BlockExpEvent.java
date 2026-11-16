package org.bukkit.event.block;

public class BlockExpEvent extends org.bukkit.event.block.BlockEvent {
    private int exp;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public BlockExpEvent(org.bukkit.block.Block p0, int p1) {
        super(p0);
        this.exp = p1;
    }

    public int getExpToDrop() {
        return exp;
    }

    public void setExpToDrop(int p0) {
        this.exp = p0;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}

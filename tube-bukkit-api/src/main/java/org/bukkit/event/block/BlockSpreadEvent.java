package org.bukkit.event.block;

public class BlockSpreadEvent extends org.bukkit.event.block.BlockFormEvent {
    private org.bukkit.block.Block source;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public BlockSpreadEvent(org.bukkit.block.Block p0, org.bukkit.block.Block p1, org.bukkit.block.BlockState p2) {
        super(p0, p2);
        this.source = p1;
    }

    public org.bukkit.block.Block getSource() {
        return source;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}

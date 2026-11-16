package org.bukkit.event.block;

public class BlockFormEvent extends org.bukkit.event.block.BlockGrowEvent {
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public BlockFormEvent(org.bukkit.block.Block p0, org.bukkit.block.BlockState p1) {
        super(p0, p1);
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}

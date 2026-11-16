package org.bukkit.event.block;

public class BlockPistonExtendEvent extends org.bukkit.event.block.BlockPistonEvent {
    private int length;
    private java.util.List<org.bukkit.block.Block> blocks;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public BlockPistonExtendEvent(org.bukkit.block.Block p0, int p1, org.bukkit.block.BlockFace p2) {
        super(p0, p2);
        this.length = p1;
    }

    public BlockPistonExtendEvent(org.bukkit.block.Block p0, java.util.List<org.bukkit.block.Block> p1, org.bukkit.block.BlockFace p2) {
        super(p0, p2);
        this.blocks = p1;
    }

    public int getLength() {
        return length;
    }

    public java.util.List<org.bukkit.block.Block> getBlocks() {
        throw new UnsupportedOperationException("BlockPistonExtendEvent#getBlocks is not supported by Tube yet");
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}

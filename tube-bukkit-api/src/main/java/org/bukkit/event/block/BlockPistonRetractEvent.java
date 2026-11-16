package org.bukkit.event.block;

public class BlockPistonRetractEvent extends org.bukkit.event.block.BlockPistonEvent {
    private java.util.List<org.bukkit.block.Block> blocks;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public BlockPistonRetractEvent(org.bukkit.block.Block p0, java.util.List<org.bukkit.block.Block> p1, org.bukkit.block.BlockFace p2) {
        super(p0, p2);
        this.blocks = p1;
    }

    public org.bukkit.Location getRetractLocation() {
        throw new UnsupportedOperationException("BlockPistonRetractEvent#getRetractLocation is not supported by Tube yet");
    }

    public java.util.List<org.bukkit.block.Block> getBlocks() {
        return blocks;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}

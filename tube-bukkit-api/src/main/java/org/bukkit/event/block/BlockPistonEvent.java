package org.bukkit.event.block;

public abstract class BlockPistonEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    private boolean cancelled;
    private org.bukkit.block.BlockFace direction;

    public BlockPistonEvent(org.bukkit.block.Block p0, org.bukkit.block.BlockFace p1) {
        super(p0);
        this.direction = p1;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean p0) {
        this.cancelled = p0;
    }

    public boolean isSticky() {
        throw new UnsupportedOperationException("BlockPistonEvent#isSticky is not supported by Tube yet");
    }

    public org.bukkit.block.BlockFace getDirection() {
        return direction;
    }
}

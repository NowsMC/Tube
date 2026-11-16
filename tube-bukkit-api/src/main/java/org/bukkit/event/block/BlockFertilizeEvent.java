package org.bukkit.event.block;

public class BlockFertilizeEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    private boolean cancelled;
    private org.bukkit.entity.Player player;
    private java.util.List<org.bukkit.block.BlockState> blocks;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public BlockFertilizeEvent(org.bukkit.block.Block p0, org.bukkit.entity.Player p1, java.util.List<org.bukkit.block.BlockState> p2) {
        super(p0);
        this.player = p1;
        this.blocks = p2;
    }

    public org.bukkit.entity.Player getPlayer() {
        return player;
    }

    public java.util.List<org.bukkit.block.BlockState> getBlocks() {
        return blocks;
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

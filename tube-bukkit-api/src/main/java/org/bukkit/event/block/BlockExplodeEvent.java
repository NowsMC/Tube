package org.bukkit.event.block;

public class BlockExplodeEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    private boolean cancel;
    private org.bukkit.block.BlockState blockState;
    private java.util.List<org.bukkit.block.Block> blocks;
    private float yield;
    private org.bukkit.ExplosionResult result;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public BlockExplodeEvent(org.bukkit.block.Block p0, org.bukkit.block.BlockState p1, java.util.List<org.bukkit.block.Block> p2, float p3, org.bukkit.ExplosionResult p4) {
        super(p0);
        this.blockState = p1;
        this.blocks = p2;
        this.yield = p3;
        this.cancel = false;
        this.result = p4;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean p0) {
        this.cancel = p0;
    }

    public org.bukkit.ExplosionResult getExplosionResult() {
        return result;
    }

    public org.bukkit.block.BlockState getExplodedBlockState() {
        return blockState;
    }

    public java.util.List<org.bukkit.block.Block> blockList() {
        return blocks;
    }

    public float getYield() {
        return yield;
    }

    public void setYield(float p0) {
        this.yield = p0;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}

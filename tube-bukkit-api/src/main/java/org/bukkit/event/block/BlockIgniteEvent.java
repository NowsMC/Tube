package org.bukkit.event.block;

public class BlockIgniteEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    private org.bukkit.event.block.BlockIgniteEvent.IgniteCause cause;
    private org.bukkit.entity.Entity ignitingEntity;
    private org.bukkit.block.Block ignitingBlock;
    private boolean cancel;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public BlockIgniteEvent(org.bukkit.block.Block p0, org.bukkit.event.block.BlockIgniteEvent.IgniteCause p1, org.bukkit.entity.Entity p2) {
        this(p0, p1, p2, (org.bukkit.block.Block) null);
    }

    public BlockIgniteEvent(org.bukkit.block.Block p0, org.bukkit.event.block.BlockIgniteEvent.IgniteCause p1, org.bukkit.block.Block p2) {
        this(p0, p1, (org.bukkit.entity.Entity) null, p2);
    }

    public BlockIgniteEvent(org.bukkit.block.Block p0, org.bukkit.event.block.BlockIgniteEvent.IgniteCause p1, org.bukkit.entity.Entity p2, org.bukkit.block.Block p3) {
        super(p0);
        this.cause = p1;
        this.ignitingEntity = p2;
        this.ignitingBlock = p3;
        this.cancel = false;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean p0) {
        this.cancel = p0;
    }

    public org.bukkit.event.block.BlockIgniteEvent.IgniteCause getCause() {
        return cause;
    }

    public org.bukkit.entity.Player getPlayer() {
        throw new UnsupportedOperationException("BlockIgniteEvent#getPlayer is not supported by Tube yet");
    }

    public org.bukkit.entity.Entity getIgnitingEntity() {
        return ignitingEntity;
    }

    public org.bukkit.block.Block getIgnitingBlock() {
        return ignitingBlock;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }

    public enum IgniteCause {
        LAVA,
        FLINT_AND_STEEL,
        SPREAD,
        LIGHTNING,
        FIREBALL,
        ENDER_CRYSTAL,
        EXPLOSION,
        ARROW
    }
}

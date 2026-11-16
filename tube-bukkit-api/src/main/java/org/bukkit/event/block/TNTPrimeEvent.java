package org.bukkit.event.block;

public class TNTPrimeEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    private boolean cancelled;
    private org.bukkit.event.block.TNTPrimeEvent.PrimeCause igniteCause;
    private org.bukkit.entity.Entity primingEntity;
    private org.bukkit.block.Block primingBlock;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public TNTPrimeEvent(org.bukkit.block.Block p0, org.bukkit.event.block.TNTPrimeEvent.PrimeCause p1, org.bukkit.entity.Entity p2, org.bukkit.block.Block p3) {
        super(p0);
        this.igniteCause = p1;
        this.primingEntity = p2;
        this.primingBlock = p3;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean p0) {
        this.cancelled = p0;
    }

    public org.bukkit.event.block.TNTPrimeEvent.PrimeCause getCause() {
        return igniteCause;
    }

    public org.bukkit.entity.Entity getPrimingEntity() {
        return primingEntity;
    }

    public org.bukkit.block.Block getPrimingBlock() {
        return primingBlock;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }

    public enum PrimeCause {
        FIRE,
        REDSTONE,
        PLAYER,
        EXPLOSION,
        PROJECTILE,
        BLOCK_BREAK,
        DISPENSER
    }
}

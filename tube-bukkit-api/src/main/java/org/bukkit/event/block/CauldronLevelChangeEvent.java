package org.bukkit.event.block;

public class CauldronLevelChangeEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    private boolean cancelled;
    private org.bukkit.entity.Entity entity;
    private org.bukkit.event.block.CauldronLevelChangeEvent.ChangeReason reason;
    private org.bukkit.block.BlockState newState;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public CauldronLevelChangeEvent(org.bukkit.block.Block p0, org.bukkit.entity.Entity p1, org.bukkit.event.block.CauldronLevelChangeEvent.ChangeReason p2, org.bukkit.block.BlockState p3) {
        super(p0);
        this.entity = p1;
        this.reason = p2;
        this.newState = p3;
    }

    public org.bukkit.entity.Entity getEntity() {
        return entity;
    }

    public org.bukkit.event.block.CauldronLevelChangeEvent.ChangeReason getReason() {
        return reason;
    }

    public org.bukkit.block.BlockState getNewState() {
        return newState;
    }

    public int getOldLevel() {
        throw new UnsupportedOperationException("CauldronLevelChangeEvent#getOldLevel is not supported by Tube yet");
    }

    public int getNewLevel() {
        throw new UnsupportedOperationException("CauldronLevelChangeEvent#getNewLevel is not supported by Tube yet");
    }

    public void setNewLevel(int p0) {
        throw new UnsupportedOperationException("CauldronLevelChangeEvent#setNewLevel is not supported by Tube yet");
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

    public enum ChangeReason {
        BUCKET_FILL,
        BUCKET_EMPTY,
        BOTTLE_FILL,
        BOTTLE_EMPTY,
        BANNER_WASH,
        ARMOR_WASH,
        SHULKER_WASH,
        EXTINGUISH,
        EVAPORATE,
        NATURAL_FILL,
        UNKNOWN
    }
}

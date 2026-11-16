package org.bukkit.event.entity;

public class EntityTransformEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    private boolean cancelled;
    private org.bukkit.entity.Entity converted;
    private java.util.List<org.bukkit.entity.Entity> convertedList;
    private org.bukkit.event.entity.EntityTransformEvent.TransformReason transformReason;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public EntityTransformEvent(org.bukkit.entity.Entity p0, java.util.List<org.bukkit.entity.Entity> p1, org.bukkit.event.entity.EntityTransformEvent.TransformReason p2) {
        super(p0);
        this.transformReason = p2;
    }

    public org.bukkit.entity.Entity getTransformedEntity() {
        return converted;
    }

    public java.util.List<org.bukkit.entity.Entity> getTransformedEntities() {
        return convertedList;
    }

    public org.bukkit.event.entity.EntityTransformEvent.TransformReason getTransformReason() {
        return transformReason;
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

    public enum TransformReason {
        CURED,
        FROZEN,
        INFECTION,
        DROWNED,
        SHEARED,
        LIGHTNING,
        SPLIT,
        PIGLIN_ZOMBIFIED,
        METAMORPHOSIS,
        UNKNOWN
    }
}

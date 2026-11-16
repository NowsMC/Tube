package org.bukkit.event.entity;

public class EntityCombustEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    private float duration;
    private boolean cancel;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public EntityCombustEvent(org.bukkit.entity.Entity p0, int p1) {
        this(p0, (float) p1);
    }

    public EntityCombustEvent(org.bukkit.entity.Entity p0, float p1) {
        super(p0);
        this.duration = p1;
        this.cancel = false;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean p0) {
        this.cancel = p0;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float p0) {
        this.duration = p0;
    }

    public void setDuration(int p0) {
        this.duration = p0;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}

package org.bukkit.event.entity;

public class EntityMountEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    private boolean cancelled;
    private org.bukkit.entity.Entity mount;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public EntityMountEvent(org.bukkit.entity.Entity p0, org.bukkit.entity.Entity p1) {
        super(p0);
        this.mount = p1;
    }

    public org.bukkit.entity.Entity getMount() {
        return mount;
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

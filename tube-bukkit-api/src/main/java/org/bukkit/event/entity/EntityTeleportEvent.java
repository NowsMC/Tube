package org.bukkit.event.entity;

public class EntityTeleportEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    private boolean cancel;
    private org.bukkit.Location from;
    private org.bukkit.Location to;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public EntityTeleportEvent(org.bukkit.entity.Entity p0, org.bukkit.Location p1, org.bukkit.Location p2) {
        super(p0);
        this.from = p1;
        this.to = p2;
        this.cancel = false;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean p0) {
        this.cancel = p0;
    }

    public org.bukkit.Location getFrom() {
        return from;
    }

    public void setFrom(org.bukkit.Location p0) {
        this.from = p0;
    }

    public org.bukkit.Location getTo() {
        return to;
    }

    public void setTo(org.bukkit.Location p0) {
        this.to = p0;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}

package org.bukkit.event.world;

public class GenericGameEvent extends org.bukkit.event.world.WorldEvent implements org.bukkit.event.Cancellable {
    private org.bukkit.Location location;
    private org.bukkit.entity.Entity entity;
    private int radius;
    private boolean cancelled;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public GenericGameEvent(org.bukkit.Location p0, org.bukkit.entity.Entity p1, int p2, boolean p3) {
        super(p0.getWorld(), p3);
        this.location = p0;
        this.entity = p1;
        this.radius = p2;
    }

    public org.bukkit.Location getLocation() {
        return location;
    }

    public org.bukkit.entity.Entity getEntity() {
        return entity;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int p0) {
        this.radius = p0;
    }

    public void setCancelled(boolean p0) {
        this.cancelled = p0;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}

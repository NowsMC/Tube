package org.bukkit.event.entity;

public class ExplosionPrimeEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    private boolean cancel;
    private float radius;
    private boolean fire;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public ExplosionPrimeEvent(org.bukkit.entity.Entity p0, float p1, boolean p2) {
        super(p0);
        this.cancel = false;
        this.radius = p1;
        this.fire = p2;
    }

    public ExplosionPrimeEvent(org.bukkit.entity.Explosive p0) {
        this((org.bukkit.entity.Entity) null, (float) 0, false);
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean p0) {
        this.cancel = p0;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float p0) {
        this.radius = p0;
    }

    public boolean getFire() {
        return fire;
    }

    public void setFire(boolean p0) {
        this.fire = p0;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}

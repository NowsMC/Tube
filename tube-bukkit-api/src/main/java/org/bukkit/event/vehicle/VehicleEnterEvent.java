package org.bukkit.event.vehicle;

public class VehicleEnterEvent extends org.bukkit.event.vehicle.VehicleEvent implements org.bukkit.event.Cancellable {
    private boolean cancelled;
    private org.bukkit.entity.Entity entered;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public VehicleEnterEvent(org.bukkit.entity.Vehicle p0, org.bukkit.entity.Entity p1) {
        super(p0);
        this.entered = p1;
    }

    public org.bukkit.entity.Entity getEntered() {
        return entered;
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

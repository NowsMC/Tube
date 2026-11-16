package org.bukkit.event.vehicle;

public class VehicleDestroyEvent extends org.bukkit.event.vehicle.VehicleEvent implements org.bukkit.event.Cancellable {
    private org.bukkit.entity.Entity attacker;
    private boolean cancelled;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public VehicleDestroyEvent(org.bukkit.entity.Vehicle p0, org.bukkit.entity.Entity p1) {
        super(p0);
        this.attacker = p1;
    }

    public org.bukkit.entity.Entity getAttacker() {
        return attacker;
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

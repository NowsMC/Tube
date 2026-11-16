package org.bukkit.event.vehicle;

public class VehicleDamageEvent extends org.bukkit.event.vehicle.VehicleEvent implements org.bukkit.event.Cancellable {
    private org.bukkit.entity.Entity attacker;
    private double damage;
    private boolean cancelled;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public VehicleDamageEvent(org.bukkit.entity.Vehicle p0, org.bukkit.entity.Entity p1, double p2) {
        super(p0);
        this.attacker = p1;
        this.damage = p2;
    }

    public org.bukkit.entity.Entity getAttacker() {
        return attacker;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double p0) {
        this.damage = p0;
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

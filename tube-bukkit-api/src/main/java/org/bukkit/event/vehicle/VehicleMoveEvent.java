package org.bukkit.event.vehicle;

public class VehicleMoveEvent extends org.bukkit.event.vehicle.VehicleEvent {
    private org.bukkit.Location from;
    private org.bukkit.Location to;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public VehicleMoveEvent(org.bukkit.entity.Vehicle p0, org.bukkit.Location p1, org.bukkit.Location p2) {
        super(p0);
        this.from = p1;
        this.to = p2;
    }

    public org.bukkit.Location getFrom() {
        return from;
    }

    public org.bukkit.Location getTo() {
        return to;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}

package org.bukkit.event.vehicle;

public abstract class VehicleEvent extends org.bukkit.event.Event {
    protected org.bukkit.entity.Vehicle vehicle;

    public VehicleEvent(org.bukkit.entity.Vehicle p0) {
        super();
        this.vehicle = p0;
    }

    public org.bukkit.entity.Vehicle getVehicle() {
        return vehicle;
    }
}

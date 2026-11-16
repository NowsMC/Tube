package org.bukkit.event.weather;

public abstract class WeatherEvent extends org.bukkit.event.Event {
    protected org.bukkit.World world;

    public WeatherEvent(org.bukkit.World p0) {
        super();
        this.world = p0;
    }

    public org.bukkit.World getWorld() {
        return world;
    }
}

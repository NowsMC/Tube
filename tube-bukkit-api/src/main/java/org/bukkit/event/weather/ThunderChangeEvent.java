package org.bukkit.event.weather;

public class ThunderChangeEvent extends org.bukkit.event.weather.WeatherEvent implements org.bukkit.event.Cancellable {
    private boolean canceled;
    private boolean to;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public ThunderChangeEvent(org.bukkit.World p0, boolean p1) {
        super(p0);
        this.to = p1;
    }

    public boolean isCancelled() {
        return canceled;
    }

    public void setCancelled(boolean p0) {
        this.canceled = p0;
    }

    public boolean toThunderState() {
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

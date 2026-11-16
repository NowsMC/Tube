package org.bukkit.event.weather;

public class LightningStrikeEvent extends org.bukkit.event.weather.WeatherEvent implements org.bukkit.event.Cancellable {
    private boolean canceled;
    private org.bukkit.entity.LightningStrike bolt;
    private org.bukkit.event.weather.LightningStrikeEvent.Cause cause;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public LightningStrikeEvent(org.bukkit.World p0, org.bukkit.entity.LightningStrike p1) {
        this(p0, p1, org.bukkit.event.weather.LightningStrikeEvent.Cause.UNKNOWN);
    }

    public LightningStrikeEvent(org.bukkit.World p0, org.bukkit.entity.LightningStrike p1, org.bukkit.event.weather.LightningStrikeEvent.Cause p2) {
        super(p0);
        this.bolt = p1;
        this.cause = p2;
    }

    public boolean isCancelled() {
        return canceled;
    }

    public void setCancelled(boolean p0) {
        this.canceled = p0;
    }

    public org.bukkit.entity.LightningStrike getLightning() {
        return bolt;
    }

    public org.bukkit.event.weather.LightningStrikeEvent.Cause getCause() {
        return cause;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }

    public enum Cause {
        COMMAND,
        CUSTOM,
        SPAWNER,
        TRIDENT,
        TRAP,
        WEATHER,
        ENCHANTMENT,
        UNKNOWN
    }
}

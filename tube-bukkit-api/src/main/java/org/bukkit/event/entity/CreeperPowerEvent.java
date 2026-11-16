package org.bukkit.event.entity;

public class CreeperPowerEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    private boolean canceled;
    private org.bukkit.event.entity.CreeperPowerEvent.PowerCause cause;
    private org.bukkit.entity.LightningStrike bolt;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public CreeperPowerEvent(org.bukkit.entity.Creeper p0, org.bukkit.entity.LightningStrike p1, org.bukkit.event.entity.CreeperPowerEvent.PowerCause p2) {
        this(p0, p2);
    }

    public CreeperPowerEvent(org.bukkit.entity.Creeper p0, org.bukkit.event.entity.CreeperPowerEvent.PowerCause p1) {
        super(p0);
        this.cause = p1;
    }

    public boolean isCancelled() {
        return canceled;
    }

    public void setCancelled(boolean p0) {
        this.canceled = p0;
    }

    public org.bukkit.entity.Creeper getEntity() {
        return (org.bukkit.entity.Creeper) entity;
    }

    public org.bukkit.entity.LightningStrike getLightning() {
        return bolt;
    }

    public org.bukkit.event.entity.CreeperPowerEvent.PowerCause getCause() {
        return cause;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }

    public enum PowerCause {
        LIGHTNING,
        SET_ON,
        SET_OFF
    }
}

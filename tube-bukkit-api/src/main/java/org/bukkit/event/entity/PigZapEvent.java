package org.bukkit.event.entity;

public class PigZapEvent extends org.bukkit.event.entity.EntityTransformEvent implements org.bukkit.event.Cancellable {
    private boolean canceled;
    private org.bukkit.entity.PigZombie pigzombie;
    private org.bukkit.entity.LightningStrike bolt;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public PigZapEvent(org.bukkit.entity.Pig p0, org.bukkit.entity.LightningStrike p1, org.bukkit.entity.PigZombie p2) {
        super(p0, java.util.List.of(p2), org.bukkit.event.entity.EntityTransformEvent.TransformReason.LIGHTNING);
        this.bolt = p1;
        this.pigzombie = p2;
    }

    public boolean isCancelled() {
        return canceled;
    }

    public void setCancelled(boolean p0) {
        this.canceled = p0;
    }

    public org.bukkit.entity.Pig getEntity() {
        return (org.bukkit.entity.Pig) entity;
    }

    public org.bukkit.entity.LightningStrike getLightning() {
        return bolt;
    }

    public org.bukkit.entity.PigZombie getPigZombie() {
        return pigzombie;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}

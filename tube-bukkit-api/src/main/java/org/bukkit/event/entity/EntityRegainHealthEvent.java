package org.bukkit.event.entity;

public class EntityRegainHealthEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    private boolean cancelled;
    private double amount;
    private org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason regainReason;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public EntityRegainHealthEvent(org.bukkit.entity.Entity p0, double p1, org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason p2) {
        super(p0);
        this.amount = p1;
        this.regainReason = p2;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double p0) {
        this.amount = p0;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean p0) {
        this.cancelled = p0;
    }

    public org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason getRegainReason() {
        return regainReason;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }

    public enum RegainReason {
        REGEN,
        SATIATED,
        EATING,
        ENDER_CRYSTAL,
        MAGIC,
        MAGIC_REGEN,
        WITHER_SPAWN,
        WITHER,
        CUSTOM
    }
}

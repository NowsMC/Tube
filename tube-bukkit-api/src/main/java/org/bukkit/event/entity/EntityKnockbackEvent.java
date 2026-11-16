package org.bukkit.event.entity;

public class EntityKnockbackEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    private org.bukkit.event.entity.EntityKnockbackEvent.KnockbackCause cause;
    private double force;
    private org.bukkit.util.Vector rawKnockback;
    private org.bukkit.util.Vector knockback;
    private boolean cancelled;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public EntityKnockbackEvent(org.bukkit.entity.LivingEntity p0, org.bukkit.event.entity.EntityKnockbackEvent.KnockbackCause p1, double p2, org.bukkit.util.Vector p3, org.bukkit.util.Vector p4) {
        super(p0);
        this.cause = p1;
        this.force = p2;
        this.rawKnockback = p3;
        this.knockback = p4;
    }

    public org.bukkit.entity.LivingEntity getEntity() {
        return (org.bukkit.entity.LivingEntity) entity;
    }

    public org.bukkit.event.entity.EntityKnockbackEvent.KnockbackCause getCause() {
        return cause;
    }

    public double getForce() {
        return force;
    }

    public org.bukkit.util.Vector getKnockback() {
        throw new UnsupportedOperationException("EntityKnockbackEvent#getKnockback is not supported by Tube yet");
    }

    public org.bukkit.util.Vector getFinalKnockback() {
        throw new UnsupportedOperationException("EntityKnockbackEvent#getFinalKnockback is not supported by Tube yet");
    }

    public void setFinalKnockback(org.bukkit.util.Vector p0) {
        throw new UnsupportedOperationException("EntityKnockbackEvent#setFinalKnockback is not supported by Tube yet");
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

    public enum KnockbackCause {
        DAMAGE,
        ENTITY_ATTACK,
        EXPLOSION,
        SHIELD_BLOCK,
        SWEEP_ATTACK,
        UNKNOWN
    }
}

package org.bukkit.event.entity;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class EntityDamageEvent extends EntityEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;
    private double damage;
    private final DamageCause cause;

    public EntityDamageEvent(Entity damagee, double damage) {
        this(damagee, DamageCause.CUSTOM, damage);
    }

    public EntityDamageEvent(Entity damagee, DamageCause cause, double damage) {
        super(damagee);
        this.damage = damage;
        this.cause = cause;
    }

    public DamageCause getCause() {
        return cause;
    }

    public enum DamageCause {
        CONTACT,
        ENTITY_ATTACK,
        ENTITY_SWEEP_ATTACK,
        PROJECTILE,
        SUFFOCATION,
        FALL,
        FIRE,
        FIRE_TICK,
        MELTING,
        LAVA,
        DROWNING,
        BLOCK_EXPLOSION,
        ENTITY_EXPLOSION,
        VOID,
        LIGHTNING,
        SUICIDE,
        STARVATION,
        POISON,
        MAGIC,
        WITHER,
        FALLING_BLOCK,
        THORNS,
        DRAGON_BREATH,
        CUSTOM,
        FLY_INTO_WALL,
        HOT_FLOOR,
        CRAMMING,
        DRYOUT,
        FREEZE,
        SONIC_BOOM
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

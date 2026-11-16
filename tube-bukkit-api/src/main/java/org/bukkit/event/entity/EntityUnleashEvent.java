package org.bukkit.event.entity;

public class EntityUnleashEvent extends org.bukkit.event.entity.EntityEvent {
    private org.bukkit.event.entity.EntityUnleashEvent.UnleashReason reason;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public EntityUnleashEvent(org.bukkit.entity.Entity p0, org.bukkit.event.entity.EntityUnleashEvent.UnleashReason p1) {
        super(p0);
        this.reason = p1;
    }

    public org.bukkit.event.entity.EntityUnleashEvent.UnleashReason getReason() {
        return reason;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }

    public enum UnleashReason {
        HOLDER_GONE,
        PLAYER_UNLEASH,
        DISTANCE,
        SHEAR,
        FIREWORK,
        UNKNOWN
    }
}

package org.bukkit.event.entity;

public class EntityRemoveEvent extends org.bukkit.event.entity.EntityEvent {
    private org.bukkit.event.entity.EntityRemoveEvent.Cause cause;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public EntityRemoveEvent(org.bukkit.entity.Entity p0, org.bukkit.event.entity.EntityRemoveEvent.Cause p1) {
        super(p0);
        this.cause = p1;
    }

    public org.bukkit.event.entity.EntityRemoveEvent.Cause getCause() {
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
        DEATH,
        DESPAWN,
        DROP,
        ENTER_BLOCK,
        EXPLODE,
        HIT,
        MERGE,
        OUT_OF_WORLD,
        PICKUP,
        PLAYER_QUIT,
        PLUGIN,
        TRANSFORMATION,
        UNLOAD
    }
}

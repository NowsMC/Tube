package org.bukkit.event.entity;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class EntityToggleGlideEvent extends EntityEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final boolean gliding;
    private boolean cancelled;

    public EntityToggleGlideEvent(LivingEntity entity, boolean isGliding) {
        super(entity);
        this.gliding = isGliding;
    }

    public boolean isGliding() {
        return gliding;
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

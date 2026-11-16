package org.bukkit.event.entity;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;

public class EntityDeathEvent extends EntityEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public EntityDeathEvent(LivingEntity entity) {
        super(entity);
    }

    @Override
    public LivingEntity getEntity() {
        return (LivingEntity) entity;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

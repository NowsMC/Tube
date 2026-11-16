package org.bukkit.event.entity;

import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class EntityPickupItemEvent extends EntityEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Item item;
    private final int remaining;
    private boolean cancelled;

    public EntityPickupItemEvent(LivingEntity entity, Item item, int remaining) {
        super(entity);
        this.item = item;
        this.remaining = remaining;
    }

    @Override
    public LivingEntity getEntity() {
        return (LivingEntity) entity;
    }

    public Item getItem() {
        return item;
    }

    public int getRemaining() {
        return remaining;
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

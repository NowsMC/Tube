package space.nows.tube.api.event;

import space.nows.tube.api.entity.ItemEntity;
import space.nows.tube.api.entity.LivingEntity;

public final class EntityPickupItemEvent implements Cancellable {

    private final LivingEntity entity;
    private final ItemEntity item;
    private boolean cancelled;

    public EntityPickupItemEvent(LivingEntity entity, ItemEntity item) {
        this.entity = entity;
        this.item = item;
    }

    public LivingEntity entity() {
        return entity;
    }

    public ItemEntity item() {
        return item;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}

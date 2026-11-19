package space.nows.tube.api.event.entity;

import space.nows.tube.api.entity.Entity;
import space.nows.tube.api.event.Cancellable;

public final class EntitySpawnEvent implements Cancellable {

    private final Entity entity;
    private boolean cancelled;

    public EntitySpawnEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity entity() {
        return entity;
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

package space.nows.tube.api.event;

import space.nows.tube.api.Entity;

public final class EntitySpawnEvent implements Event {

    private final Entity entity;

    public EntitySpawnEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity entity() {
        return entity;
    }
}

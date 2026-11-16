package space.nows.tube.api.event;

import space.nows.tube.api.entity.LivingEntity;

public final class EntityDeathEvent implements Event {

    private final LivingEntity entity;

    public EntityDeathEvent(LivingEntity entity) {
        this.entity = entity;
    }

    public LivingEntity entity() {
        return entity;
    }
}

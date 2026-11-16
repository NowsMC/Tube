package space.nows.tube.api.event;

import space.nows.tube.api.LivingEntity;

public final class EntityDamageEvent implements Cancellable {

    private final LivingEntity entity;
    private final double damage;
    private boolean cancelled;

    public EntityDamageEvent(LivingEntity entity, double damage) {
        this.entity = entity;
        this.damage = damage;
    }

    public LivingEntity entity() {
        return entity;
    }

    public double damage() {
        return damage;
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

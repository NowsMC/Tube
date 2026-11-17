package space.nows.tube.api.event;

import space.nows.tube.api.entity.Entity;
import space.nows.tube.api.entity.LivingEntity;

import java.util.Optional;

public final class EntityDamageEvent implements Cancellable {

    private final LivingEntity entity;
    private final String cause;
    private final Entity attacker;
    private double damage;
    private boolean cancelled;

    public EntityDamageEvent(LivingEntity entity, double damage) {
        this(entity, damage, "minecraft:generic");
    }

    public EntityDamageEvent(LivingEntity entity, double damage, String cause) {
        this(entity, damage, cause, null);
    }

    public EntityDamageEvent(LivingEntity entity, double damage, String cause, Entity attacker) {
        this.entity = entity;
        this.damage = damage;
        this.cause = cause;
        this.attacker = attacker;
    }

    public String cause() {
        return cause;
    }

    public LivingEntity entity() {
        return entity;
    }

    public Optional<Entity> attacker() {
        return Optional.ofNullable(attacker);
    }

    public double damage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = Math.max(0, damage);
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

package space.nows.tube.api.entity;

import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.api.world.Location;

import java.util.Optional;

public interface Mob extends LivingEntity {

    Brain brain();

    Optional<LivingEntity> target();

    void setTarget(LivingEntity target);

    boolean moveTo(Location location, double speed);

    boolean moveTo(Entity entity, double speed);

    void stopMoving();

    boolean isMoving();

    void lookAt(Entity entity);

    void lookAt(Location location);

    void attack(Entity target);

    boolean isBaby();

    boolean isAggressive();

    void setAggressive(boolean aggressive);

    boolean isPersistent();

    void setPersistent(boolean persistent);
}

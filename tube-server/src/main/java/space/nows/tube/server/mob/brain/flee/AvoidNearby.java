package space.nows.tube.server.mob.brain.flee;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import space.nows.tube.api.entity.mob.brain.Behavior;
import space.nows.tube.api.entity.mob.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.brain.target.Targeting;
import space.nows.tube.server.mob.move.RandomSpot;

import java.util.function.Predicate;

public final class AvoidNearby extends Behavior {

    private static final double SEARCH_HEIGHT = 3.0;
    private static final double SPRINT_WITHIN_SQUARED = 49.0;

    private final Creature creature;
    private final Predicate<Entity> kind;
    private final Predicate<LivingEntity> condition;
    private final float distance;
    private final double walkSpeed;
    private final double sprintSpeed;
    private LivingEntity avoided;
    private Point refuge;

    public AvoidNearby(Creature creature, Predicate<Entity> kind, Predicate<LivingEntity> condition, float distance, double walkSpeed, double sprintSpeed) {
        super(Control.MOVE);
        this.creature = creature;
        this.kind = kind;
        this.condition = condition;
        this.distance = distance;
        this.walkSpeed = walkSpeed;
        this.sprintSpeed = sprintSpeed;
    }

    public static AvoidNearby type(Creature creature, EntityType type, float distance, double walkSpeed, double sprintSpeed) {
        return new AvoidNearby(creature, entity -> entity.getEntityType() == type, entity -> true, distance, walkSpeed, sprintSpeed);
    }

    @Override
    public boolean canStart() {
        avoided = Targeting.combat(distance).only(condition).nearest(creature, distance, SEARCH_HEIGHT, kind);
        if (avoided == null) {
            return false;
        }
        refuge = RandomSpot.awayFrom(creature, 16, 7, avoided.getPosition());
        return refuge != null && avoided.getPosition().distanceSquared(refuge) >= avoided.getPosition().distanceSquared(creature.getPosition());
    }

    @Override
    public boolean canContinue() {
        return !creature.navigation().isDone();
    }

    @Override
    public void start() {
        creature.navigation().moveTo(refuge, walkSpeed);
    }

    @Override
    public void stop() {
        avoided = null;
    }

    @Override
    public void tick() {
        boolean close = creature.getPosition().distanceSquared(avoided.getPosition()) < SPRINT_WITHIN_SQUARED;
        creature.navigation().setSpeed(close ? sprintSpeed : walkSpeed);
    }
}

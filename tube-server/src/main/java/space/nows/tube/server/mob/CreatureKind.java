package space.nows.tube.server.mob;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityType;
import space.nows.tube.server.mob.ai.GoalSelector;

import java.util.random.RandomGenerator;

public interface CreatureKind {

    EntityType type();

    CreatureStats stats();

    Disposition disposition();

    void configure(Creature creature, GoalSelector goals, GoalSelector targets);

    default float walkPreference(Creature creature, Point at) {
        return 0f;
    }

    default void prepare(Creature creature, RandomGenerator random) {
    }

    default boolean ambientSounds() {
        return true;
    }

    default int ambientInterval() {
        return 80;
    }

    default float headTurnSpeed() {
        return 10f;
    }

    default float maxHeadPitch() {
        return 40f;
    }

    default float maxHeadTurn() {
        return 75f;
    }
}

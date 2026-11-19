package space.nows.tube.server.mob;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import space.nows.tube.api.entity.mob.brain.Brain;

import java.util.random.RandomGenerator;

public interface CreatureKind {

    EntityType type();

    CreatureStats stats();

    Disposition disposition();

    void configure(Creature creature, Brain brain);

    default float walkPreference(Creature creature, Point at) {
        return 0f;
    }

    default void prepare(Creature creature, RandomGenerator random) {
    }

    default boolean acceptsDamage(Creature creature, Damage damage) {
        return true;
    }

    default void absorb(Creature creature, Damage damage) {
    }

    default void damaged(Creature creature, Damage damage) {
    }

    default void died(Creature creature) {
    }

    default float attackDamage(Creature creature) {
        return (float) creature.getAttributeValue(Attribute.ATTACK_DAMAGE);
    }

    default void landedHit(Creature creature, LivingEntity target) {
    }

    default String voice() {
        return type().key().value();
    }

    default boolean fireproof() {
        return false;
    }

    default boolean leavesWhenFar(Creature creature) {
        return true;
    }

    default int despawnRange() {
        return 128;
    }

    default boolean aquatic() {
        return false;
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

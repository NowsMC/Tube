package space.nows.tube.server.mob.kind.flier;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.attribute.Attribute;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureKind;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.Disposition;
import space.nows.tube.server.mob.brain.idle.FloatAround;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.move.FloatControl;

public final class Allay implements CreatureKind {

    private static final CreatureStats STATS = CreatureStats.mob(20, 0.1).withDamage(2);
    private static final double FLYING_SPEED = 0.05;
    private static final int RANGE = 8;

    @Override
    public EntityType type() {
        return EntityType.ALLAY;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public Disposition disposition() {
        return Disposition.PASSIVE;
    }

    @Override
    public boolean leavesWhenFar(Creature creature) {
        return false;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        creature.getAttribute(Attribute.FLYING_SPEED).setBaseValue(FLYING_SPEED);
        FloatControl control = new FloatControl(creature);
        creature.addTrait(control);
        brain.add(3, new FloatAround(creature, control, RANGE));
        brain.add(4, WatchNearby.players(creature, 8f));
    }
}

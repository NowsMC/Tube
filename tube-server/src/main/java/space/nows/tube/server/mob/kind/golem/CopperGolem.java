package space.nows.tube.server.mob.kind.golem;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.attribute.Attribute;
import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.Disposition;
import space.nows.tube.server.mob.brain.idle.LookAround;
import space.nows.tube.server.mob.brain.idle.Panic;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;

public final class CopperGolem extends Golem {

    private static final CreatureStats STATS = CreatureStats.mob(12, 0.2);

    @Override
    public EntityType type() {
        return EntityType.COPPER_GOLEM;
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
    public void configure(Creature creature, Brain brain) {
        creature.getAttribute(Attribute.STEP_HEIGHT).setBaseValue(1);
        brain.add(1, new Panic(creature, 1.5));
        brain.add(5, Wander.onLand(creature, 1.0));
        brain.add(6, WatchNearby.players(creature, 6f));
        brain.add(7, new LookAround(creature));
    }
}

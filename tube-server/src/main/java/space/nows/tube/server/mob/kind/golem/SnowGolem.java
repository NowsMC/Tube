package space.nows.tube.server.mob.kind.golem;

import net.minestom.server.entity.EntityType;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.Disposition;
import space.nows.tube.server.mob.brain.combat.RangedAttack;
import space.nows.tube.server.mob.brain.idle.LookAround;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.target.HuntNearest;

public final class SnowGolem extends Golem {

    private static final CreatureStats STATS = CreatureStats.mob(4, 0.2);

    @Override
    public EntityType type() {
        return EntityType.SNOW_GOLEM;
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
        brain.add(1, new RangedAttack(creature, 1.25, 20, 10f, new SnowballVolley()));
        brain.add(2, Wander.onLandOnly(creature, 1.0));
        brain.add(3, WatchNearby.players(creature, 6f));
        brain.add(4, new LookAround(creature));
        brain.add(1, HuntNearest.of(creature, Golem::enemy, 10, true, target -> true));
        creature.addTrait(new Snowfall(creature));
    }
}

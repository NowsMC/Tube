package space.nows.tube.server.mob.kind.villager;

import net.minestom.server.entity.EntityType;
import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.flee.AvoidNearby;
import space.nows.tube.server.mob.brain.idle.Panic;
import space.nows.tube.server.mob.brain.idle.StayAfloat;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.trait.Lifespan;

import java.util.random.RandomGenerator;

public final class WanderingTrader extends Villager {

    private static final CreatureStats STATS = CreatureStats.mob(20, 0.7).withFollowRange(16);
    private static final int STAY = 48000;

    @Override
    public EntityType type() {
        return EntityType.WANDERING_TRADER;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    protected double stroll() {
        return 0.35;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        brain.add(0, new StayAfloat(creature));
        brain.add(1, new Panic(creature, 0.5));
        brain.add(1, new AvoidNearby(creature, Villager::danger, target -> true, 8f, 0.5, 0.5));
        brain.add(8, Wander.onLand(creature, stroll()));
        brain.add(10, WatchNearby.players(creature, 8f));
        creature.addTrait(new Lifespan(creature, STAY));
    }

    @Override
    public void prepare(Creature creature, RandomGenerator random) {
    }
}

package space.nows.tube.server.mob.kind.villager;

import net.minestom.server.entity.EntityType;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
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
        super.configure(creature, brain);
        creature.addTrait(new Lifespan(creature, STAY));
    }

    @Override
    public void prepare(Creature creature, RandomGenerator random) {
    }
}

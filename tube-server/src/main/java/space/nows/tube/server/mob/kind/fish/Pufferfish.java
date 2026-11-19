package space.nows.tube.server.mob.kind.fish;

import net.minestom.server.entity.EntityType;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;

public final class Pufferfish extends Fish {

    private static final CreatureStats STATS = CreatureStats.mob(3, 0.1);

    @Override
    public EntityType type() {
        return EntityType.PUFFERFISH;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    protected int schoolSize() {
        return 0;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        super.configure(creature, brain);
        Puffing puffing = new Puffing(creature);
        creature.addTrait(puffing);
        brain.add(1, new Puff(creature, puffing));
    }
}

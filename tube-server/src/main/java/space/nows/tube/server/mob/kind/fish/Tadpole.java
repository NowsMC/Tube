package space.nows.tube.server.mob.kind.fish;

import net.minestom.server.entity.EntityType;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.trait.Conversion;

public final class Tadpole extends Fish {

    private static final CreatureStats STATS = CreatureStats.mob(6, 1.0);
    private static final int GROW_UP = 24000;

    @Override
    public EntityType type() {
        return EntityType.TADPOLE;
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
        creature.addTrait(new Conversion(creature, EntityType.FROG, GROW_UP, tadpole -> true));
    }
}

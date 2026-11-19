package space.nows.tube.server.mob.kind.monster;

import net.minestom.server.entity.EntityType;
import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;

public final class Giant extends Monster {

    private static final CreatureStats STATS = CreatureStats.monster(100, 0.5).withDamage(50);

    @Override
    public EntityType type() {
        return EntityType.GIANT;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
    }
}

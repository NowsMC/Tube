package space.nows.tube.server.mob.kind.fish;

import net.minestom.server.entity.EntityType;
import space.nows.tube.server.mob.CreatureStats;

public final class Cod extends Fish {

    private static final CreatureStats STATS = CreatureStats.mob(3, 0.1);

    @Override
    public EntityType type() {
        return EntityType.COD;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }
}

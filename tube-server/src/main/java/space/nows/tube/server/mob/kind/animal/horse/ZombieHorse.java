package space.nows.tube.server.mob.kind.animal.horse;

import net.minestom.server.entity.EntityType;
import space.nows.tube.server.mob.CreatureStats;

public final class ZombieHorse extends Equine {

    private static final CreatureStats STATS = CreatureStats.animal(25, 0.225);

    @Override
    public EntityType type() {
        return EntityType.ZOMBIE_HORSE;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    protected boolean panics() {
        return false;
    }
}

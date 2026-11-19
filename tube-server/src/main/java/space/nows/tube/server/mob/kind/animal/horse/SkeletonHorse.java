package space.nows.tube.server.mob.kind.animal.horse;

import net.minestom.server.entity.EntityType;
import space.nows.tube.server.mob.CreatureStats;

public final class SkeletonHorse extends Equine {

    private static final CreatureStats STATS = CreatureStats.animal(15, 0.2);

    @Override
    public EntityType type() {
        return EntityType.SKELETON_HORSE;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    protected boolean floats() {
        return false;
    }

    @Override
    protected boolean panics() {
        return false;
    }

    @Override
    protected boolean tempted() {
        return false;
    }
}

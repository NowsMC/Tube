package space.nows.tube.server.mob.kind.monster.cube;

import net.minestom.server.entity.EntityType;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.Disposition;

public final class SulfurCube extends Cube {

    private static final CreatureStats STATS = CreatureStats.mob(1, 0.3).withTemptRange(8);

    @Override
    public EntityType type() {
        return EntityType.SULFUR_CUBE;
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
    protected boolean hunts() {
        return false;
    }

    @Override
    protected void sized(Creature cube, int size) {
    }

    @Override
    boolean harmful(Creature cube) {
        return false;
    }

    @Override
    float squashDamage(Creature cube) {
        return 0;
    }
}

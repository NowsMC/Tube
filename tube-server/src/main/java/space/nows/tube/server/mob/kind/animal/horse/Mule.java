package space.nows.tube.server.mob.kind.animal.horse;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.attribute.Attribute;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;

import java.util.random.RandomGenerator;

public final class Mule extends Equine {

    private static final CreatureStats STATS = CreatureStats.animal(53, 0.175);
    private static final double JUMP = 0.5;

    @Override
    public EntityType type() {
        return EntityType.MULE;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void prepare(Creature creature, RandomGenerator random) {
        rollHealth(creature, random);
        creature.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(JUMP);
    }
}

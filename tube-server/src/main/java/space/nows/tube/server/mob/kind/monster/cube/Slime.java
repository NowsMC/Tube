package space.nows.tube.server.mob.kind.monster.cube;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.attribute.Attribute;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;

public final class Slime extends Cube {

    private static final CreatureStats STATS = CreatureStats.monster(1, 0.3);

    @Override
    public EntityType type() {
        return EntityType.SLIME;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    protected void sized(Creature cube, int size) {
        cube.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(size);
    }

    @Override
    boolean harmful(Creature cube) {
        return size(cube) > 1;
    }

    @Override
    float squashDamage(Creature cube) {
        return (float) cube.getAttributeValue(Attribute.ATTACK_DAMAGE);
    }
}

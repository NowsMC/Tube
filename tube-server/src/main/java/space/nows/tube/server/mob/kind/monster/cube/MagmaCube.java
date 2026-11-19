package space.nows.tube.server.mob.kind.monster.cube;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.attribute.Attribute;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;

public final class MagmaCube extends Cube {

    private static final CreatureStats STATS = CreatureStats.monster(1, 0.2);
    private static final int ARMOR_PER_SIZE = 3;
    private static final float EXTRA_DAMAGE = 2;
    private static final int SLOWER_JUMPS = 4;
    private static final double JUMP_PER_SIZE = 0.1;

    @Override
    public EntityType type() {
        return EntityType.MAGMA_CUBE;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public boolean fireproof() {
        return true;
    }

    @Override
    protected void sized(Creature cube, int size) {
        cube.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(size);
        cube.getAttribute(Attribute.ARMOR).setBaseValue(size * ARMOR_PER_SIZE);
    }

    @Override
    boolean harmful(Creature cube) {
        return true;
    }

    @Override
    float squashDamage(Creature cube) {
        return (float) cube.getAttributeValue(Attribute.ATTACK_DAMAGE) + EXTRA_DAMAGE;
    }

    @Override
    int jumpDelay(Creature cube) {
        return super.jumpDelay(cube) * SLOWER_JUMPS;
    }

    @Override
    double jumpPower(Creature cube) {
        return super.jumpPower(cube) + size(cube) * JUMP_PER_SIZE;
    }
}

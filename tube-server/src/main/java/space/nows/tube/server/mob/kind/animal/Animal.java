package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureKind;
import space.nows.tube.server.mob.Disposition;
import space.nows.tube.server.mob.sense.Daylight;

abstract class Animal implements CreatureKind {

    private static final float GRASS_PREFERENCE = 10f;

    @Override
    public int ambientInterval() {
        return 120;
    }

    @Override
    public Disposition disposition() {
        return Disposition.PASSIVE;
    }

    @Override
    public float walkPreference(Creature creature, Point at) {
        Instance instance = creature.getInstance();
        if (instance.getBlock(at.blockX(), at.blockY() - 1, at.blockZ()).compare(Block.GRASS_BLOCK)) {
            return GRASS_PREFERENCE;
        }
        return Daylight.pathCost(instance, at);
    }
}

package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.component.DataComponent;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureKind;
import space.nows.tube.server.mob.Disposition;
import space.nows.tube.server.mob.life.Breedable;
import space.nows.tube.server.mob.life.Lifecycle;
import space.nows.tube.server.mob.sense.Daylight;

import java.util.random.RandomGenerator;

abstract class Animal implements CreatureKind, Breedable {

    private static final float GRASS_PREFERENCE = 10f;

    static Lifecycle lifecycle(Creature creature) {
        Lifecycle life = new Lifecycle(creature);
        creature.addTrait(life);
        return life;
    }

    static <T> void inheritComponent(Creature child, Creature mother, Creature father, DataComponent<T> component, RandomGenerator random) {
        T value = (random.nextBoolean() ? mother : father).get(component);
        if (value != null) {
            child.set(component, value);
        }
    }

    @Override
    public int ambientInterval() {
        return 120;
    }

    @Override
    public boolean leavesWhenFar(Creature creature) {
        return false;
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

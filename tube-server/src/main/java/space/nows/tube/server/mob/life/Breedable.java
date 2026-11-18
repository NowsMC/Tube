package space.nows.tube.server.mob.life;

import net.minestom.server.item.Material;
import space.nows.tube.server.mob.Creature;

import java.util.Set;
import java.util.random.RandomGenerator;

public interface Breedable {

    Set<Material> breedingFood();

    default void inherit(Creature child, Creature mother, Creature father, RandomGenerator random) {
    }
}

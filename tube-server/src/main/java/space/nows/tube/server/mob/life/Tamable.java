package space.nows.tube.server.mob.life;

import net.minestom.server.item.Material;
import space.nows.tube.server.mob.Creature;

import java.util.Set;

public interface Tamable {

    Set<Material> tamingFood();

    int tameOneIn();

    default void tamed(Creature creature) {
    }
}

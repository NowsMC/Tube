package space.nows.tube.server.gameplay;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import space.nows.tube.server.gameplay.block.BlockDropper;

final class Drops {

    private Drops() {
    }

    static void spawn(Instance instance, Point position, ItemStack item) {
        BlockDropper.spawn(instance, position, item);
    }
}

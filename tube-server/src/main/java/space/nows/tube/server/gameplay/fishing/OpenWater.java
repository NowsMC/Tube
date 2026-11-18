package space.nows.tube.server.gameplay.fishing;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import space.nows.tube.server.mob.move.Liquids;

final class OpenWater {

    private static final int RADIUS = 2;
    private static final int BELOW = 1;
    private static final int ABOVE = 2;
    private static final int WATER_SHARE_DIVISOR = 3;

    private OpenWater() {
    }

    static boolean at(Instance instance, Point bobber) {
        int water = 0;
        int total = 0;
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -BELOW; dy <= ABOVE; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    Block block = instance.getBlock(bobber.blockX() + dx, bobber.blockY() + dy, bobber.blockZ() + dz);
                    if (Liquids.isWater(block)) {
                        water++;
                    } else if (!block.air()) {
                        return false;
                    }
                    total++;
                }
            }
        }
        return water >= total / WATER_SHARE_DIVISOR;
    }
}

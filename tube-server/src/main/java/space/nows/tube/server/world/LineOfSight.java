package space.nows.tube.server.world;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public final class LineOfSight {

    private static final double STEP = 0.25;

    private LineOfSight() {
    }

    public static boolean clear(Instance instance, Point from, Point to) {
        Vec path = Vec.fromPoint(to.sub(from));
        double length = path.length();
        if (length < STEP) {
            return true;
        }
        Vec step = path.normalize().mul(STEP);
        Point at = from;
        for (double travelled = STEP; travelled < length; travelled += STEP) {
            at = at.add(step);
            if (!instance.isChunkLoaded(at.chunkX(), at.chunkZ())) {
                return false;
            }
            Block block = instance.getBlock(at);
            if (block.solid()) {
                return false;
            }
        }
        return true;
    }
}

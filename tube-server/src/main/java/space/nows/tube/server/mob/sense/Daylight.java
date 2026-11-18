package space.nows.tube.server.mob.sense;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;

public final class Daylight {

    private static final int MAX_SKY_DARKEN = 11;
    private static final int BRIGHT_OUTSIDE_BELOW = 4;
    private static final long DAY_LENGTH = 24000;

    private Daylight() {
    }

    public static int skyDarken(Instance instance) {
        if (!instance.getCachedDimensionType().hasSkylight()) {
            return MAX_SKY_DARKEN;
        }
        double fraction = Math.floorMod(instance.getTime(), DAY_LENGTH) / (double) DAY_LENGTH - 0.25;
        fraction = fraction - Math.floor(fraction);
        double angle = (fraction * 2 + (0.5 - Math.cos(fraction * Math.PI) / 2)) / 3;
        double light = Math.clamp(0.5 + 2 * Math.cos(angle * Math.PI * 2), 0.0, 1.0);
        return (int) ((1 - light) * MAX_SKY_DARKEN);
    }

    public static boolean brightOutside(Instance instance) {
        return instance.getCachedDimensionType().hasSkylight() && skyDarken(instance) < BRIGHT_OUTSIDE_BELOW;
    }

    public static int rawBrightness(Instance instance, Point at) {
        int sky = instance.getCachedDimensionType().hasSkylight() ? instance.getSkyLight(at.blockX(), at.blockY(), at.blockZ()) - skyDarken(instance) : 0;
        return Math.max(sky, instance.getBlockLight(at.blockX(), at.blockY(), at.blockZ()));
    }

    public static float brightness(Instance instance, Point at) {
        float ambient = instance.getCachedDimensionType().ambientLight();
        float level = Math.clamp(rawBrightness(instance, at), 0, 15) / 15f;
        float curve = level / (4f - 3f * level);
        return ambient + (1f - ambient) * curve;
    }

    public static float pathCost(Instance instance, Point at) {
        return brightness(instance, at) - 0.5f;
    }

    public static boolean seesSky(Instance instance, Point at) {
        int maxY = instance.getCachedDimensionType().maxY();
        for (int y = at.blockY() + 1; y < maxY; y++) {
            Block block = instance.getBlock(at.blockX(), y, at.blockZ());
            if (block.liquid() || !block.air() && block.collisionShape().isFaceFull(BlockFace.BOTTOM)) {
                return false;
            }
        }
        return true;
    }
}

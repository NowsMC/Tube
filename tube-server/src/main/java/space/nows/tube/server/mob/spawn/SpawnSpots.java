package space.nows.tube.server.mob.spawn;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.world.DimensionType;

final class SpawnSpots {

    private SpawnSpots() {
    }

    static boolean overworld(Instance instance) {
        DimensionType dimension = instance.getCachedDimensionType();
        return dimension.hasSkylight() && !dimension.hasCeiling() && !dimension.hasEnderDragonFight();
    }

    static boolean standable(Instance instance, int x, int y, int z) {
        Block ground = instance.getBlock(x, y - 1, z);
        Block feet = instance.getBlock(x, y, z);
        Block head = instance.getBlock(x, y + 1, z);
        return ground.collisionShape().isFaceFull(BlockFace.TOP) && !ground.liquid()
                && !feet.solid() && !feet.liquid() && !head.solid() && !head.liquid();
    }
}

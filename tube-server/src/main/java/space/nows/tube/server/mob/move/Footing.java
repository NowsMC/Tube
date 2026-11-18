package space.nows.tube.server.mob.move;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;

import java.util.Set;

public final class Footing {

    private static final Set<Block> HARMFUL = Set.of(Block.FIRE, Block.SOUL_FIRE, Block.LAVA, Block.MAGMA_BLOCK,
            Block.CACTUS, Block.SWEET_BERRY_BUSH, Block.POWDER_SNOW, Block.CAMPFIRE, Block.SOUL_CAMPFIRE, Block.WITHER_ROSE);

    private Footing() {
    }

    public static boolean stable(Instance instance, Point at) {
        Block below = instance.getBlock(at.blockX(), at.blockY() - 1, at.blockZ());
        return below.collisionShape().isFaceFull(BlockFace.TOP) && below.occludes();
    }

    public static boolean solid(Instance instance, Point at) {
        return instance.getBlock(at).solid();
    }

    public static boolean penalized(Instance instance, Point at) {
        Block feet = instance.getBlock(at);
        Block below = instance.getBlock(at.blockX(), at.blockY() - 1, at.blockZ());
        return feet.liquid() || Liquids.isWater(feet) || harmful(feet) || harmful(below) || feet.solid();
    }

    public static boolean water(Instance instance, Point at) {
        return Liquids.isWater(instance.getBlock(at));
    }

    private static boolean harmful(Block block) {
        for (Block danger : HARMFUL) {
            if (block.compare(danger)) {
                return true;
            }
        }
        return false;
    }
}

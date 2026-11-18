package space.nows.tube.server.mob.move;

import net.minestom.server.instance.block.Block;

public final class Liquids {

    private Liquids() {
    }

    public static boolean isWater(Block block) {
        return block.compare(Block.WATER) || "true".equals(block.getProperty("waterlogged"))
                || block.compare(Block.BUBBLE_COLUMN) || block.compare(Block.KELP) || block.compare(Block.KELP_PLANT)
                || block.compare(Block.SEAGRASS) || block.compare(Block.TALL_SEAGRASS);
    }

    public static boolean isLava(Block block) {
        return block.compare(Block.LAVA);
    }
}

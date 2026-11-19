package space.nows.tube.server.world.effect;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public final class BlockBreakEffect {

    private BlockBreakEffect() {
    }

    public static void play(Instance instance, Point at, Block block) {
        WorldEffect.BLOCK_BREAK.play(instance, at, block.stateId());
    }
}

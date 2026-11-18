package space.nows.tube.server.world.effect;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.WorldEventPacket;

public final class BlockBreakEffect {

    private static final int BLOCK_BREAK_EVENT = 2001;

    private BlockBreakEffect() {
    }

    public static void play(Instance instance, Point at, Block block) {
        instance.sendGroupedPacket(new WorldEventPacket(BLOCK_BREAK_EVENT, at, block.stateId(), false));
    }
}

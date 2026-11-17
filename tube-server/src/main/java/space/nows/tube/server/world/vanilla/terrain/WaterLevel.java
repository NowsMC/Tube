package space.nows.tube.server.world.vanilla.terrain;

import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.block.BlockStates;

record WaterLevel(int surface, BlockState fluid) {

    static final int NONE = -2_032_128;

    BlockState blockAt(int y) {
        return y < surface ? fluid : BlockStates.AIR;
    }
}

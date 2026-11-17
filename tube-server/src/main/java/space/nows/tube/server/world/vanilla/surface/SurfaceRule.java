package space.nows.tube.server.world.vanilla.surface;

import space.nows.tube.server.world.vanilla.block.BlockState;

@FunctionalInterface
interface SurfaceRule {

    BlockState paint(SurfaceColumn column);
}

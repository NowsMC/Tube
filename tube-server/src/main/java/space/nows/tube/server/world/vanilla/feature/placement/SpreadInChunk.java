package space.nows.tube.server.world.vanilla.feature.placement;

import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

record SpreadInChunk() implements PlacementStep {

    @Override
    public void apply(DecorationPass pass, Xoroshiro random, int x, int y, int z, Next next) {
        int offsetX = random.nextInt(16);
        int offsetZ = random.nextInt(16);
        next.at(x + offsetX, y, z + offsetZ);
    }
}

package space.nows.tube.server.world.vanilla.feature.placement;

import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.feature.test.BlockTest;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

record BlockFilter(BlockTest test) implements PlacementStep {

    @Override
    public void apply(DecorationPass pass, Xoroshiro random, int x, int y, int z, Next next) {
        if (test.test(pass.area, x, y, z)) {
            next.at(x, y, z);
        }
    }
}

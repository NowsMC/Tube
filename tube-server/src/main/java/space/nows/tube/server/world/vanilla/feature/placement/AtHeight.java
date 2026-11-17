package space.nows.tube.server.world.vanilla.feature.placement;

import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.feature.sample.HeightSampler;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

record AtHeight(HeightSampler height) implements PlacementStep {

    @Override
    public void apply(DecorationPass pass, Xoroshiro random, int x, int y, int z, Next next) {
        next.at(x, height.sample(random), z);
    }
}

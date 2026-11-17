package space.nows.tube.server.world.vanilla.feature.placement;

import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.feature.sample.IntSampler;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

record Jitter(IntSampler horizontal, IntSampler vertical) implements PlacementStep {

    @Override
    public void apply(DecorationPass pass, Xoroshiro random, int x, int y, int z, Next next) {
        int offsetX = horizontal.sample(random);
        int offsetY = vertical.sample(random);
        int offsetZ = horizontal.sample(random);
        next.at(x + offsetX, y + offsetY, z + offsetZ);
    }
}

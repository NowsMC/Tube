package space.nows.tube.server.world.vanilla.feature.placement;

import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.feature.sample.IntSampler;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

record Repeat(IntSampler count) implements PlacementStep {

    @Override
    public void apply(DecorationPass pass, Xoroshiro random, int x, int y, int z, Next next) {
        int times = count.sample(random);
        for (int i = 0; i < times; i++) {
            next.at(x, y, z);
        }
    }
}

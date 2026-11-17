package space.nows.tube.server.world.vanilla.feature.placement;

import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

@FunctionalInterface
public interface PlacementStep {

    @FunctionalInterface
    interface Next {
        void at(int x, int y, int z);
    }

    void apply(DecorationPass pass, Xoroshiro random, int x, int y, int z, Next next);
}

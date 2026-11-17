package space.nows.tube.server.world.vanilla.feature;

import space.nows.tube.server.world.vanilla.random.Xoroshiro;

@FunctionalInterface
public interface Feature {

    Feature NONE = (pass, random, x, y, z) -> false;

    boolean place(DecorationPass pass, Xoroshiro random, int x, int y, int z);
}

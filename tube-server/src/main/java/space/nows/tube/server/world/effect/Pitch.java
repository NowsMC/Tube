package space.nows.tube.server.world.effect;

import java.util.random.RandomGenerator;

public final class Pitch {

    private Pitch() {
    }

    public static float wobble(RandomGenerator random) {
        return (random.nextFloat() - random.nextFloat()) * 0.2f + 1f;
    }

    public static float thrown(RandomGenerator random) {
        return 0.4f / (random.nextFloat() * 0.4f + 0.8f);
    }

    public static float between(RandomGenerator random, float min, float max) {
        return min + random.nextFloat() * (max - min);
    }
}

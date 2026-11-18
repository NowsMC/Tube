package space.nows.tube.server.loot.number;

import java.util.random.RandomGenerator;

public interface NumberProvider {

    double sample(RandomGenerator random);

    default int sampleInt(RandomGenerator random) {
        return (int) Math.floor(sample(random));
    }
}

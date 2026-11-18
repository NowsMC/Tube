package space.nows.tube.server.loot.number;

import java.util.random.RandomGenerator;

record Uniform(NumberProvider min, NumberProvider max) implements NumberProvider {

    @Override
    public double sample(RandomGenerator random) {
        double low = min.sample(random);
        return low + random.nextDouble() * (max.sample(random) - low);
    }

    @Override
    public int sampleInt(RandomGenerator random) {
        int low = min.sampleInt(random);
        return low + random.nextInt(max.sampleInt(random) - low + 1);
    }
}

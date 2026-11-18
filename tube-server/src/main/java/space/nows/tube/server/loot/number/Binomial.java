package space.nows.tube.server.loot.number;

import java.util.random.RandomGenerator;

record Binomial(NumberProvider trials, NumberProvider chance) implements NumberProvider {

    @Override
    public double sample(RandomGenerator random) {
        return sampleInt(random);
    }

    @Override
    public int sampleInt(RandomGenerator random) {
        int count = trials.sampleInt(random);
        double probability = chance.sample(random);
        int hits = 0;
        for (int i = 0; i < count; i++) {
            if (random.nextDouble() < probability) {
                hits++;
            }
        }
        return hits;
    }
}

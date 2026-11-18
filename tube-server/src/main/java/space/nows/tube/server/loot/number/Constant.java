package space.nows.tube.server.loot.number;

import java.util.random.RandomGenerator;

record Constant(double value) implements NumberProvider {

    @Override
    public double sample(RandomGenerator random) {
        return value;
    }
}

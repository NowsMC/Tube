package space.nows.tube.api.world;

import java.util.Objects;

public record WorldOptions(String generator, long seed, Environment environment) {

    public static final String FLAT = "tube:flat";
    public static final String VOID = "tube:void";
    public static final String NORMAL = "tube:normal";

    public WorldOptions {
        Objects.requireNonNull(generator, "generator");
        Objects.requireNonNull(environment, "environment");
    }

    public static WorldOptions of(String generator, long seed) {
        return new WorldOptions(generator, seed, Environment.OVERWORLD);
    }

    public WorldOptions withGenerator(String generator) {
        return new WorldOptions(generator, seed, environment);
    }

    public WorldOptions withSeed(long seed) {
        return new WorldOptions(generator, seed, environment);
    }

    public WorldOptions withEnvironment(Environment environment) {
        return new WorldOptions(generator, seed, environment);
    }
}

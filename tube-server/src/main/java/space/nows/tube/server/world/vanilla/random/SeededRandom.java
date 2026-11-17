package space.nows.tube.server.world.vanilla.random;

public interface SeededRandom {

    long nextLong();

    int nextInt(int bound);

    float nextFloat();

    double nextDouble();

    boolean nextBoolean();

    double nextGaussian();

    default int nextBetween(int min, int max) {
        return min + nextInt(max - min + 1);
    }
}

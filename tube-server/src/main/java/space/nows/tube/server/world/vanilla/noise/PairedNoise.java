package space.nows.tube.server.world.vanilla.noise;

import space.nows.tube.server.world.vanilla.random.Xoroshiro;

public final class PairedNoise {

    private static final double SECOND_SCALE = 1.0181268882175227;

    private final OctaveNoise first;
    private final OctaveNoise second;
    private final double normalizer;

    public PairedNoise(Xoroshiro random, int firstOctave, double[] amplitudes) {
        this.first = OctaveNoise.named(random, firstOctave, amplitudes);
        this.second = OctaveNoise.named(random, firstOctave, amplitudes);
        this.normalizer = (1.0 / 6.0) / expectedDeviation(span(amplitudes));
    }

    private static int span(double[] amplitudes) {
        int lowest = Integer.MAX_VALUE;
        int highest = Integer.MIN_VALUE;
        for (int i = 0; i < amplitudes.length; i++) {
            if (amplitudes[i] != 0) {
                lowest = Math.min(lowest, i);
                highest = Math.max(highest, i);
            }
        }
        return highest - lowest;
    }

    private static double expectedDeviation(int octaveSpan) {
        return 0.1 * (1 + 1.0 / (octaveSpan + 1));
    }

    public double value(double x, double y, double z) {
        double a = first.value(x, y, z);
        double b = second.value(x * SECOND_SCALE, y * SECOND_SCALE, z * SECOND_SCALE);
        return (a + b) * normalizer;
    }
}

package space.nows.tube.server.world.vanilla.noise;

import space.nows.tube.server.world.vanilla.random.PositionalRandom;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.Arrays;

public final class OctaveNoise {

    private final GradientNoise[] octaves;
    private final double[] amplitudes;
    private final double coarsestFrequency;
    private final double coarsestWeight;

    private OctaveNoise(GradientNoise[] octaves, double[] amplitudes, int firstOctave) {
        this.octaves = octaves;
        this.amplitudes = amplitudes;
        this.coarsestFrequency = Math.pow(2, firstOctave);
        this.coarsestWeight = Math.pow(2, amplitudes.length - 1) / (Math.pow(2, amplitudes.length) - 1);
    }

    public static OctaveNoise named(Xoroshiro random, int firstOctave, double[] amplitudes) {
        PositionalRandom names = random.split();
        GradientNoise[] octaves = new GradientNoise[amplitudes.length];
        for (int i = 0; i < amplitudes.length; i++) {
            if (amplitudes[i] != 0) {
                octaves[i] = new GradientNoise(names.named("octave_" + (firstOctave + i)));
            }
        }
        return new OctaveNoise(octaves, amplitudes, firstOctave);
    }

    public static OctaveNoise sequential(Xoroshiro random, int firstOctave, int count) {
        double[] amplitudes = new double[count];
        Arrays.fill(amplitudes, 1.0);
        GradientNoise[] octaves = new GradientNoise[count];
        for (int i = -firstOctave; i >= 0; i--) {
            octaves[i] = new GradientNoise(random);
        }
        return new OctaveNoise(octaves, amplitudes, firstOctave);
    }

    public double value(double x, double y, double z) {
        double total = 0;
        double frequency = coarsestFrequency;
        double weight = coarsestWeight;
        for (int i = 0; i < octaves.length; i++) {
            GradientNoise octave = octaves[i];
            if (octave != null) {
                double sample = octave.sample(NoiseMath.wrap(x * frequency), NoiseMath.wrap(y * frequency), NoiseMath.wrap(z * frequency));
                total += amplitudes[i] * sample * weight;
            }
            frequency *= 2;
            weight /= 2;
        }
        return total;
    }

    GradientNoise finest(int step) {
        return octaves[octaves.length - 1 - step];
    }
}

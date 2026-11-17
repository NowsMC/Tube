package space.nows.tube.server.world.vanilla.noise;

import space.nows.tube.server.world.vanilla.random.Xoroshiro;

public final class TerrainBlendNoise {

    private static final double BASE_SCALE = 684.412;
    private static final int SELECTOR_OCTAVES = 8;
    private static final int LIMIT_OCTAVES = 16;

    private final OctaveNoise lowerLimit;
    private final OctaveNoise upperLimit;
    private final OctaveNoise selector;
    private final double xzScale;
    private final double yScale;
    private final double xzFactor;
    private final double yFactor;
    private final double smear;

    public TerrainBlendNoise(Xoroshiro random, double xzScale, double yScale, double xzFactor, double yFactor, double smear) {
        this.lowerLimit = OctaveNoise.sequential(random, -15, LIMIT_OCTAVES);
        this.upperLimit = OctaveNoise.sequential(random, -15, LIMIT_OCTAVES);
        this.selector = OctaveNoise.sequential(random, -7, SELECTOR_OCTAVES);
        this.xzScale = BASE_SCALE * xzScale;
        this.yScale = BASE_SCALE * yScale;
        this.xzFactor = xzFactor;
        this.yFactor = yFactor;
        this.smear = smear;
    }

    public double value(int blockX, int blockY, int blockZ) {
        double x = blockX * xzScale;
        double y = blockY * yScale;
        double z = blockZ * xzScale;
        double ySmear = yScale * smear;
        double mix = (selectorValue(x / xzFactor, y / yFactor, z / xzFactor, ySmear / yFactor) / 10 + 1) / 2;
        double lower = mix >= 1 ? 0 : limitValue(lowerLimit, x, y, z, ySmear);
        double upper = mix <= 0 ? 0 : limitValue(upperLimit, x, y, z, ySmear);
        double low = lower / 512;
        double high = upper / 512;
        double blended = mix < 0 ? low : mix > 1 ? high : low + mix * (high - low);
        return blended / 128;
    }

    private double selectorValue(double x, double y, double z, double ySmear) {
        double total = 0;
        double scale = 1;
        for (int step = 0; step < SELECTOR_OCTAVES; step++) {
            GradientNoise octave = selector.finest(step);
            if (octave != null) {
                total += octave.sample(NoiseMath.wrap(x * scale), NoiseMath.wrap(y * scale), NoiseMath.wrap(z * scale), ySmear * scale, y * scale) / scale;
            }
            scale /= 2;
        }
        return total;
    }

    private static double limitValue(OctaveNoise limit, double x, double y, double z, double ySmear) {
        double total = 0;
        double scale = 1;
        for (int step = 0; step < LIMIT_OCTAVES; step++) {
            GradientNoise octave = limit.finest(step);
            if (octave != null) {
                total += octave.sample(NoiseMath.wrap(x * scale), NoiseMath.wrap(y * scale), NoiseMath.wrap(z * scale), ySmear * scale, y * scale) / scale;
            }
            scale /= 2;
        }
        return total;
    }
}

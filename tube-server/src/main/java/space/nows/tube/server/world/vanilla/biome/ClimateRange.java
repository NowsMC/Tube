package space.nows.tube.server.world.vanilla.biome;

public record ClimateRange(long min, long max) {

    public static final ClimateRange FULL = of(-1.0F, 1.0F);

    public static ClimateRange of(float min, float max) {
        return new ClimateRange(quantize(min), quantize(max));
    }

    public static ClimateRange at(float value) {
        return of(value, value);
    }

    public ClimateRange to(ClimateRange other) {
        return new ClimateRange(min, other.max);
    }

    public boolean negative() {
        return max < 0;
    }

    public static long quantize(float value) {
        return (long) (value * 10000.0F);
    }
}

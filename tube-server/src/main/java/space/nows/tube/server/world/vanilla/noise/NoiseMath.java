package space.nows.tube.server.world.vanilla.noise;

public final class NoiseMath {

    private static final double WRAP_PERIOD = 33554432.0;

    private NoiseMath() {
    }

    public static int floor(double value) {
        int truncated = (int) value;
        return value < truncated ? truncated - 1 : truncated;
    }

    public static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    public static double lerp(double t, double from, double to) {
        return from + t * (to - from);
    }

    public static double wrap(double value) {
        return value - Math.floor(value / WRAP_PERIOD + 0.5) * WRAP_PERIOD;
    }
}

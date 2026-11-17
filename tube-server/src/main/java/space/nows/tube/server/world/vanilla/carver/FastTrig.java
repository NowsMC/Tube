package space.nows.tube.server.world.vanilla.carver;

final class FastTrig {

    private static final int STEPS = 65536;
    private static final double STEPS_PER_RADIAN = 10430.378350470453;
    private static final float[] SINE = new float[STEPS];

    static {
        for (int i = 0; i < STEPS; i++) {
            SINE[i] = (float) Math.sin(i * Math.PI * 2.0 / STEPS);
        }
    }

    private FastTrig() {
    }

    static float sin(double radians) {
        return SINE[(int) ((long) (radians * STEPS_PER_RADIAN) & (STEPS - 1))];
    }

    static float cos(double radians) {
        return SINE[(int) ((long) (radians * STEPS_PER_RADIAN + STEPS / 4.0) & (STEPS - 1))];
    }
}

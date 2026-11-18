package space.nows.tube.server.mob.look;

public final class Angles {

    private Angles() {
    }

    public static float wrap(float degrees) {
        float wrapped = degrees % 360f;
        if (wrapped >= 180f) {
            wrapped -= 360f;
        }
        if (wrapped < -180f) {
            wrapped += 360f;
        }
        return wrapped;
    }

    public static float turn(float from, float to, float maxStep) {
        float step = Math.clamp(wrap(to - from), -maxStep, maxStep);
        return from + step;
    }

    public static float within(float angle, float center, float maxOffset) {
        float offset = wrap(angle - center);
        if (offset < -maxOffset) {
            return center - maxOffset;
        }
        if (offset > maxOffset) {
            return center + maxOffset;
        }
        return angle;
    }
}

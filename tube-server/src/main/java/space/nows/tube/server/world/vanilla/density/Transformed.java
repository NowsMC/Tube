package space.nows.tube.server.world.vanilla.density;

import java.util.function.UnaryOperator;

public record Transformed(Shape shape, Density input) implements Density {

    public enum Shape {
        ABS, SQUARE, CUBE, HALF_NEGATIVE, QUARTER_NEGATIVE, SQUEEZE, INVERT
    }

    @Override
    public double at(int x, int y, int z) {
        double value = input.at(x, y, z);
        return switch (shape) {
            case ABS -> Math.abs(value);
            case SQUARE -> value * value;
            case CUBE -> value * value * value;
            case HALF_NEGATIVE -> value > 0 ? value : value * 0.5;
            case QUARTER_NEGATIVE -> value > 0 ? value : value * 0.25;
            case SQUEEZE -> squeeze(value);
            case INVERT -> 1 / value;
        };
    }

    private static double squeeze(double value) {
        double clamped = Math.clamp(value, -1.0, 1.0);
        return clamped / 2 - clamped * clamped * clamped / 24;
    }

    @Override
    public Density rebuild(UnaryOperator<Density> children) {
        return new Transformed(shape, children.apply(input));
    }
}

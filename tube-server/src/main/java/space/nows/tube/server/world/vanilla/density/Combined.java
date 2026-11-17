package space.nows.tube.server.world.vanilla.density;

import java.util.function.UnaryOperator;

public record Combined(Operator operator, Density left, Density right) implements Density {

    public enum Operator {
        ADD, MULTIPLY, MIN, MAX
    }

    @Override
    public double at(int x, int y, int z) {
        double first = left.at(x, y, z);
        return switch (operator) {
            case ADD -> first + right.at(x, y, z);
            case MULTIPLY -> first == 0 ? 0 : first * right.at(x, y, z);
            case MIN -> Math.min(first, right.at(x, y, z));
            case MAX -> Math.max(first, right.at(x, y, z));
        };
    }

    @Override
    public Density rebuild(UnaryOperator<Density> children) {
        return new Combined(operator, children.apply(left), children.apply(right));
    }
}

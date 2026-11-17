package space.nows.tube.server.world.vanilla.density;

import java.util.function.UnaryOperator;

public record Clamped(Density input, double min, double max) implements Density {

    @Override
    public double at(int x, int y, int z) {
        return Math.clamp(input.at(x, y, z), min, max);
    }

    @Override
    public Density rebuild(UnaryOperator<Density> children) {
        return new Clamped(children.apply(input), min, max);
    }
}

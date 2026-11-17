package space.nows.tube.server.world.vanilla.density;

import java.util.function.UnaryOperator;

public record RangeSwitch(Density input, double from, double below, Density inside, Density outside) implements Density {

    @Override
    public double at(int x, int y, int z) {
        double value = input.at(x, y, z);
        return value >= from && value < below ? inside.at(x, y, z) : outside.at(x, y, z);
    }

    @Override
    public Density rebuild(UnaryOperator<Density> children) {
        return new RangeSwitch(children.apply(input), from, below, children.apply(inside), children.apply(outside));
    }
}

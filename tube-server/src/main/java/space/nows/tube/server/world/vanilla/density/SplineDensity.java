package space.nows.tube.server.world.vanilla.density;

import java.util.function.UnaryOperator;

public record SplineDensity(Curve curve) implements Density {

    @Override
    public double at(int x, int y, int z) {
        return curve.at(x, y, z);
    }

    @Override
    public Density rebuild(UnaryOperator<Density> children) {
        return new SplineDensity(curve.rebuild(children));
    }
}

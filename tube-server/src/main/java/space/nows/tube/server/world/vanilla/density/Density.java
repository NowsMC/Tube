package space.nows.tube.server.world.vanilla.density;

import java.util.function.UnaryOperator;

public interface Density {

    double at(int x, int y, int z);

    default Density rebuild(UnaryOperator<Density> children) {
        return this;
    }
}

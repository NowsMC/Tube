package space.nows.tube.server.world.vanilla.density;

import java.util.function.UnaryOperator;

public record CacheHint(Kind kind, Density input) implements Density {

    public enum Kind {
        CELL_INTERPOLATED, QUART_COLUMN, COLUMN, POINT, CELL
    }

    @Override
    public double at(int x, int y, int z) {
        if (kind == Kind.QUART_COLUMN) {
            return input.at(x >> 2 << 2, 0, z >> 2 << 2);
        }
        return input.at(x, y, z);
    }

    @Override
    public Density rebuild(UnaryOperator<Density> children) {
        return new CacheHint(kind, children.apply(input));
    }
}

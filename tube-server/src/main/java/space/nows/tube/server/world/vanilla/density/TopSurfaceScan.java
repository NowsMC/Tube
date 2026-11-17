package space.nows.tube.server.world.vanilla.density;

import java.util.function.UnaryOperator;

public record TopSurfaceScan(Density density, Density upperBound, int lowerBound, int step) implements Density {

    @Override
    public double at(int x, int y, int z) {
        int start = Math.floorDiv((int) Math.floor(upperBound.at(x, y, z)), step) * step;
        for (int scanY = start; scanY >= lowerBound; scanY -= step) {
            if (density.at(x, scanY, z) > 0) {
                return scanY;
            }
        }
        return lowerBound;
    }

    @Override
    public Density rebuild(UnaryOperator<Density> children) {
        return new TopSurfaceScan(children.apply(density), children.apply(upperBound), lowerBound, step);
    }
}

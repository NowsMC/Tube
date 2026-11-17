package space.nows.tube.server.world.vanilla.density;

public record ConstantDensity(double value) implements Density {

    @Override
    public double at(int x, int y, int z) {
        return value;
    }
}

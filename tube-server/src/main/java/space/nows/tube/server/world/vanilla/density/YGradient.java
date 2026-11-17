package space.nows.tube.server.world.vanilla.density;

public record YGradient(int fromY, int toY, double fromValue, double toValue) implements Density {

    @Override
    public double at(int x, int y, int z) {
        if (y <= fromY) {
            return fromValue;
        }
        if (y >= toY) {
            return toValue;
        }
        return fromValue + (double) (y - fromY) / (toY - fromY) * (toValue - fromValue);
    }
}

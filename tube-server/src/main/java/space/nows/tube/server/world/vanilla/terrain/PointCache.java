package space.nows.tube.server.world.vanilla.terrain;

import space.nows.tube.server.world.vanilla.density.Density;

final class PointCache implements Density {

    private final Density input;
    private int lastX = Integer.MIN_VALUE;
    private int lastY;
    private int lastZ;
    private double value;

    PointCache(Density input) {
        this.input = input;
    }

    @Override
    public double at(int x, int y, int z) {
        if (x != lastX || y != lastY || z != lastZ) {
            value = input.at(x, y, z);
            lastX = x;
            lastY = y;
            lastZ = z;
        }
        return value;
    }
}

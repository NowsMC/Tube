package space.nows.tube.server.world.vanilla.terrain;

import space.nows.tube.server.world.vanilla.density.Density;

final class ColumnCache implements Density {

    private final Density input;
    private int lastX = Integer.MIN_VALUE;
    private int lastZ = Integer.MIN_VALUE;
    private double value;

    ColumnCache(Density input) {
        this.input = input;
    }

    @Override
    public double at(int x, int y, int z) {
        if (x != lastX || z != lastZ) {
            value = input.at(x, y, z);
            lastX = x;
            lastZ = z;
        }
        return value;
    }
}

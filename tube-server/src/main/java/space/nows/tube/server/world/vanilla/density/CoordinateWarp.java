package space.nows.tube.server.world.vanilla.density;

import space.nows.tube.server.world.vanilla.noise.PairedNoise;

public record CoordinateWarp(Axes axes, PairedNoise noise) implements Density {

    public enum Axes {
        XZ, ZX, XYZ
    }

    private static final double SCALE = 0.25;
    private static final double STRENGTH = 4;

    @Override
    public double at(int x, int y, int z) {
        return switch (axes) {
            case XZ -> noise.value(x * SCALE, 0, z * SCALE) * STRENGTH;
            case ZX -> noise.value(z * SCALE, x * SCALE, 0) * STRENGTH;
            case XYZ -> noise.value(x * SCALE, y * SCALE, z * SCALE) * STRENGTH;
        };
    }
}

package space.nows.tube.server.world.vanilla.terrain;

import space.nows.tube.server.world.vanilla.density.Density;

import java.util.Arrays;

final class QuartColumnCache implements Density {

    private static final int QUARTS = 5;

    private final Density input;
    private final ChunkDensity chunk;
    private final double[] values = new double[QUARTS * QUARTS];

    QuartColumnCache(Density input, ChunkDensity chunk) {
        this.input = input;
        this.chunk = chunk;
        Arrays.fill(values, Double.NaN);
    }

    @Override
    public double at(int x, int y, int z) {
        int quartX = (x >> 2) - (chunk.minX >> 2);
        int quartZ = (z >> 2) - (chunk.minZ >> 2);
        if (quartX < 0 || quartX >= QUARTS || quartZ < 0 || quartZ >= QUARTS) {
            return input.at(x >> 2 << 2, 0, z >> 2 << 2);
        }
        int slot = quartX * QUARTS + quartZ;
        double value = values[slot];
        if (Double.isNaN(value)) {
            value = input.at(x >> 2 << 2, 0, z >> 2 << 2);
            values[slot] = value;
        }
        return value;
    }
}

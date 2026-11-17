package space.nows.tube.server.world.vanilla.noise;

import space.nows.tube.server.world.vanilla.random.SeededRandom;

public final class GradientNoise {

    private static final int[][] DIRECTIONS = {
            {1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0},
            {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1},
            {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1},
            {1, 1, 0}, {0, -1, 1}, {-1, 1, 0}, {0, -1, -1}
    };

    private final double originX;
    private final double originY;
    private final double originZ;
    private final byte[] shuffle = new byte[256];

    public GradientNoise(SeededRandom random) {
        originX = random.nextDouble() * 256;
        originY = random.nextDouble() * 256;
        originZ = random.nextDouble() * 256;
        for (int i = 0; i < shuffle.length; i++) {
            shuffle[i] = (byte) i;
        }
        for (int i = 0; i < shuffle.length; i++) {
            int other = i + random.nextInt(shuffle.length - i);
            byte swap = shuffle[i];
            shuffle[i] = shuffle[other];
            shuffle[other] = swap;
        }
    }

    public double sample(double x, double y, double z) {
        return sample(x, y, z, 0, 0);
    }

    public double sample(double x, double y, double z, double yStep, double yLimit) {
        double px = x + originX;
        double py = y + originY;
        double pz = z + originZ;
        int cellX = NoiseMath.floor(px);
        int cellY = NoiseMath.floor(py);
        int cellZ = NoiseMath.floor(pz);
        double localX = px - cellX;
        double localY = py - cellY;
        double localZ = pz - cellZ;
        double snap = 0;
        if (yStep != 0) {
            double limited = yLimit >= 0 && yLimit < localY ? yLimit : localY;
            snap = NoiseMath.floor(limited / yStep + 1.0E-7F) * yStep;
        }
        return blend(cellX, cellY, cellZ, localX, localY - snap, localZ, localY);
    }

    private int hash(int value) {
        return shuffle[value & 255] & 255;
    }

    private double blend(int cellX, int cellY, int cellZ, double x, double y, double z, double fadeY) {
        int x0 = hash(cellX);
        int x1 = hash(cellX + 1);
        int x0y0 = hash(x0 + cellY);
        int x0y1 = hash(x0 + cellY + 1);
        int x1y0 = hash(x1 + cellY);
        int x1y1 = hash(x1 + cellY + 1);
        double sx = NoiseMath.fade(x);
        double sy = NoiseMath.fade(fadeY);
        double sz = NoiseMath.fade(z);
        double near = NoiseMath.lerp(sy,
                NoiseMath.lerp(sx, dot(hash(x0y0 + cellZ), x, y, z), dot(hash(x1y0 + cellZ), x - 1, y, z)),
                NoiseMath.lerp(sx, dot(hash(x0y1 + cellZ), x, y - 1, z), dot(hash(x1y1 + cellZ), x - 1, y - 1, z)));
        double far = NoiseMath.lerp(sy,
                NoiseMath.lerp(sx, dot(hash(x0y0 + cellZ + 1), x, y, z - 1), dot(hash(x1y0 + cellZ + 1), x - 1, y, z - 1)),
                NoiseMath.lerp(sx, dot(hash(x0y1 + cellZ + 1), x, y - 1, z - 1), dot(hash(x1y1 + cellZ + 1), x - 1, y - 1, z - 1)));
        return NoiseMath.lerp(sz, near, far);
    }

    private static double dot(int hash, double x, double y, double z) {
        int[] direction = DIRECTIONS[hash & 15];
        return direction[0] * x + direction[1] * y + direction[2] * z;
    }
}

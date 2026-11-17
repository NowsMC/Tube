package space.nows.tube.server.world.generator;

import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;

final class CaveNoise {

    private static final int STEP = 4;

    private final FastSimplexNoiseGenerator noise;
    private final double horizontalScale;
    private final double verticalScale;

    CaveNoise(long seed, double horizontalScale, double verticalScale) {
        this.noise = FastSimplexNoiseGenerator.newBuilder().setSeed(seed).build();
        this.horizontalScale = horizontalScale;
        this.verticalScale = verticalScale;
    }

    double sample(double x, double y, double z) {
        return noise.evaluateNoise(x / horizontalScale, y / verticalScale, z / horizontalScale);
    }

    double[][][] grid(int baseX, int baseZ, int minY, int maxY) {
        int cellsY = (maxY - minY) / STEP + 1;
        double[][][] grid = new double[16 / STEP + 1][16 / STEP + 1][cellsY];
        for (int gx = 0; gx <= 16 / STEP; gx++) {
            for (int gz = 0; gz <= 16 / STEP; gz++) {
                for (int gy = 0; gy < cellsY; gy++) {
                    grid[gx][gz][gy] = sample(baseX + gx * STEP, minY + gy * STEP, baseZ + gz * STEP);
                }
            }
        }
        return grid;
    }

    static double at(double[][][] grid, int x, int y, int z, int minY) {
        int gx = x / STEP;
        int gz = z / STEP;
        int gy = (y - minY) / STEP;
        double fx = (x % STEP) / (double) STEP;
        double fz = (z % STEP) / (double) STEP;
        double fy = ((y - minY) % STEP) / (double) STEP;
        int gy1 = Math.min(gy + 1, grid[0][0].length - 1);
        double c00 = lerp(grid[gx][gz][gy], grid[gx + 1][gz][gy], fx);
        double c10 = lerp(grid[gx][gz + 1][gy], grid[gx + 1][gz + 1][gy], fx);
        double c01 = lerp(grid[gx][gz][gy1], grid[gx + 1][gz][gy1], fx);
        double c11 = lerp(grid[gx][gz + 1][gy1], grid[gx + 1][gz + 1][gy1], fx);
        return lerp(lerp(c00, c10, fz), lerp(c01, c11, fz), fy);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}

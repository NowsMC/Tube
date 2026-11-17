package space.nows.tube.server.world.vanilla.surface;

import space.nows.tube.server.world.vanilla.noise.PairedNoise;
import space.nows.tube.server.world.vanilla.random.PositionalRandom;
import space.nows.tube.server.world.vanilla.terrain.ChunkDensity;
import space.nows.tube.server.world.vanilla.terrain.ChunkDraft;

final class SurfaceColumn {

    private static final int UNKNOWN = Integer.MIN_VALUE;

    private final ChunkDraft draft;
    private final ChunkDensity density;
    private final int[] tops;
    private final PairedNoise depthNoise;
    private final PairedNoise secondaryNoise;
    private final PositionalRandom depthJitter;

    int localX;
    int localZ;
    int x;
    int y;
    int z;
    int surfaceDepth;
    int stoneAbove;
    int stoneBelow;
    int waterTop;
    private double secondary;
    private int lowestSurface;
    private int steep;
    private String biome;
    private int biomeY;

    SurfaceColumn(ChunkDraft draft, ChunkDensity density, int[] tops, PairedNoise depthNoise, PairedNoise secondaryNoise,
                  PositionalRandom depthJitter) {
        this.draft = draft;
        this.density = density;
        this.tops = tops;
        this.depthNoise = depthNoise;
        this.secondaryNoise = secondaryNoise;
        this.depthJitter = depthJitter;
    }

    void moveTo(int localX, int localZ) {
        this.localX = localX;
        this.localZ = localZ;
        this.x = draft.minX() + localX;
        this.z = draft.minZ() + localZ;
        this.surfaceDepth = (int) (depthNoise.value(x, 0, z) * 2.75 + 3.0 + depthJitter.at(x, 0, z).nextDouble() * 0.25);
        this.secondary = Double.NaN;
        this.lowestSurface = UNKNOWN;
        this.steep = -1;
        this.biome = null;
    }

    void moveDown(int y, int stoneAbove, int stoneBelow, int waterTop) {
        this.y = y;
        this.stoneAbove = stoneAbove;
        this.stoneBelow = stoneBelow;
        this.waterTop = waterTop;
    }

    String biome() {
        if (biome == null || biomeY != y) {
            biome = draft.biome(localX, y, localZ);
            biomeY = y;
        }
        return biome;
    }

    double secondary() {
        if (Double.isNaN(secondary)) {
            secondary = secondaryNoise.value(x, 0, z);
        }
        return secondary;
    }

    int lowestSurface() {
        if (lowestSurface == UNKNOWN) {
            int cornerX = x >> 4 << 4;
            int cornerZ = z >> 4 << 4;
            double northWest = density.surfaceEstimate(cornerX, cornerZ);
            double northEast = density.surfaceEstimate(cornerX + 16, cornerZ);
            double southWest = density.surfaceEstimate(cornerX, cornerZ + 16);
            double southEast = density.surfaceEstimate(cornerX + 16, cornerZ + 16);
            double fx = (x & 15) / 16.0;
            double fz = (z & 15) / 16.0;
            double north = northWest + fx * (northEast - northWest);
            double south = southWest + fx * (southEast - southWest);
            lowestSurface = (int) Math.floor(north + fz * (south - north)) + surfaceDepth - 8;
        }
        return lowestSurface;
    }

    boolean steep() {
        if (steep < 0) {
            int west = top(localX - 1, localZ);
            int east = top(localX + 1, localZ);
            int north = top(localX, localZ - 1);
            int south = top(localX, localZ + 1);
            steep = east >= west + 4 || north >= south + 4 ? 1 : 0;
        }
        return steep == 1;
    }

    private int top(int localX, int localZ) {
        return tops[Math.clamp(localZ, 0, 15) << 4 | Math.clamp(localX, 0, 15)];
    }
}

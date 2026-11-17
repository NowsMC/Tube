package space.nows.tube.server.world.vanilla.terrain;

import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.block.BlockStates;
import space.nows.tube.server.world.vanilla.density.Density;

final class WaterLevelPicker {

    private static final int LAVA_SEA = -54;
    private static final int[][] NEIGHBOUR_CHUNKS = {
            {0, 0}, {-2, -1}, {-1, -1}, {0, -1}, {1, -1}, {-3, 0}, {-2, 0}, {-1, 0}, {1, 0}, {-2, 1}, {-1, 1}, {0, 1}, {1, 1}
    };

    private final ChunkDensity chunk;
    private final Density floodedness;
    private final Density spread;
    private final Density lavaNoise;
    private final Density erosion;
    private final Density depth;
    private final WaterLevel lavaSea;
    private final WaterLevel sea;
    private final int lavaBelow;

    WaterLevelPicker(TerrainSettings settings, ChunkDensity chunk) {
        TerrainRouter router = settings.router();
        this.chunk = chunk;
        this.floodedness = chunk.localize(router.floodedness());
        this.spread = chunk.localize(router.fluidSpread());
        this.lavaNoise = chunk.localize(router.lava());
        this.erosion = chunk.localize(router.erosion());
        this.depth = chunk.localize(router.depth());
        this.lavaSea = new WaterLevel(LAVA_SEA, BlockStates.LAVA);
        this.sea = new WaterLevel(settings.seaLevel(), settings.sea());
        this.lavaBelow = Math.min(LAVA_SEA, settings.seaLevel());
    }

    WaterLevel sea() {
        return sea;
    }

    WaterLevel global(int y) {
        return y < lavaBelow ? lavaSea : sea;
    }

    WaterLevel pick(int x, int y, int z) {
        WaterLevel global = global(y);
        int lowestSurface = Integer.MAX_VALUE;
        boolean underOcean = false;
        for (int[] offset : NEIGHBOUR_CHUNKS) {
            int surface = chunk.surfaceEstimate(x + (offset[0] << 4), z + (offset[1] << 4));
            int reachLimit = surface + 8;
            boolean own = offset[0] == 0 && offset[1] == 0;
            if (own && y - 12 > reachLimit) {
                return global;
            }
            boolean reaches = y + 12 > reachLimit;
            if ((reaches || own) && !global(reachLimit).blockAt(reachLimit).air) {
                underOcean |= own;
                if (reaches) {
                    return global(reachLimit);
                }
            }
            lowestSurface = Math.min(lowestSurface, surface);
        }
        int surface = surfaceFor(x, y, z, global, lowestSurface, underOcean);
        return new WaterLevel(surface, fluidFor(x, y, z, global, surface));
    }

    private int surfaceFor(int x, int y, int z, WaterLevel global, int lowestSurface, boolean underOcean) {
        if (erosion.at(x, y, z) < -0.225 && depth.at(x, y, z) > 0.9) {
            return WaterLevel.NONE;
        }
        double nearSurface = underOcean ? Math.clamp(1.0 - (lowestSurface + 8 - y) / 64.0, 0.0, 1.0) : 0;
        double flood = Math.clamp(floodedness.at(x, y, z), -1.0, 1.0);
        double fullThreshold = -0.3 + (1 - nearSurface) * 1.1;
        double partialThreshold = -0.8 + (1 - nearSurface) * 1.2;
        if (flood - fullThreshold > 0) {
            return global.surface();
        }
        if (flood - partialThreshold > 0) {
            return randomizedSurface(x, y, z, lowestSurface);
        }
        return WaterLevel.NONE;
    }

    private int randomizedSurface(int x, int y, int z, int lowestSurface) {
        int band = Math.floorDiv(y, 40);
        double wobble = spread.at(Math.floorDiv(x, 16), band, Math.floorDiv(z, 16)) * 10;
        int stepped = (int) Math.floor(wobble / 3) * 3;
        return Math.min(lowestSurface, band * 40 + 20 + stepped);
    }

    private BlockState fluidFor(int x, int y, int z, WaterLevel global, int surface) {
        if (surface <= -10 && surface != WaterLevel.NONE && !global.fluid().lava) {
            double value = lavaNoise.at(Math.floorDiv(x, 64), Math.floorDiv(y, 40), Math.floorDiv(z, 64));
            if (Math.abs(value) > 0.3) {
                return BlockStates.LAVA;
            }
        }
        return global.fluid();
    }
}

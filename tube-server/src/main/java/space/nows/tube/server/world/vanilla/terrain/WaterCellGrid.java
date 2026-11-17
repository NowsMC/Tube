package space.nows.tube.server.world.vanilla.terrain;

import space.nows.tube.server.world.vanilla.random.PositionalRandom;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.Arrays;

final class WaterCellGrid {

    static final int WIDTH = 16;
    static final int TALL = 12;
    private static final int UNSET = Integer.MIN_VALUE;

    private final PositionalRandom random;
    private final WaterLevelPicker picker;
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int sizeX;
    private final int sizeZ;
    private final int[] centers;
    private final WaterLevel[] levels;
    private final byte[] seaOnly;

    WaterCellGrid(ChunkDensity chunk, PositionalRandom random, WaterLevelPicker picker) {
        this.random = random;
        this.picker = picker;
        this.minX = Math.floorDiv(chunk.minX - 5, WIDTH);
        this.minZ = Math.floorDiv(chunk.minZ - 5, WIDTH);
        this.minY = Math.floorDiv(chunk.minY + 1, TALL) - 1;
        this.sizeX = Math.floorDiv(chunk.minX + 10, WIDTH) + 2 - minX;
        this.sizeZ = Math.floorDiv(chunk.minZ + 10, WIDTH) + 2 - minZ;
        int sizeY = Math.floorDiv(chunk.minY + chunk.height, TALL) + 2 - minY;
        int cells = sizeX * sizeY * sizeZ;
        this.centers = new int[cells * 3];
        this.levels = new WaterLevel[cells];
        this.seaOnly = new byte[cells];
        Arrays.fill(centers, UNSET);
    }

    int cell(int gridX, int gridY, int gridZ) {
        int cell = ((gridY - minY) * sizeZ + gridZ - minZ) * sizeX + gridX - minX;
        int offset = cell * 3;
        if (centers[offset] == UNSET) {
            Xoroshiro jitter = random.at(gridX, gridY, gridZ);
            centers[offset] = gridX * WIDTH + jitter.nextInt(10);
            centers[offset + 1] = gridY * TALL + jitter.nextInt(9);
            centers[offset + 2] = gridZ * WIDTH + jitter.nextInt(10);
        }
        return cell;
    }

    int distanceSquared(int cell, int x, int y, int z) {
        int dx = centers[cell * 3] - x;
        int dy = centers[cell * 3 + 1] - y;
        int dz = centers[cell * 3 + 2] - z;
        return dx * dx + dy * dy + dz * dz;
    }

    WaterLevel level(int cell) {
        WaterLevel level = levels[cell];
        if (level == null) {
            level = picker.pick(centers[cell * 3], centers[cell * 3 + 1], centers[cell * 3 + 2]);
            levels[cell] = level;
        }
        return level;
    }

    boolean onlySea(int gridX, int gridY, int gridZ) {
        int group = cell(gridX, gridY, gridZ);
        if (seaOnly[group] == 0) {
            seaOnly[group] = allSea(gridX, gridY, gridZ) ? (byte) 1 : (byte) 2;
        }
        return seaOnly[group] == 1;
    }

    private boolean allSea(int gridX, int gridY, int gridZ) {
        for (int dx = 0; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = 0; dz <= 1; dz++) {
                    if (level(cell(gridX + dx, gridY + dy, gridZ + dz)) != picker.sea()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}

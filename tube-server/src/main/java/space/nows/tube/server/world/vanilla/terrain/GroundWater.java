package space.nows.tube.server.world.vanilla.terrain;

import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.density.Density;
import space.nows.tube.server.world.vanilla.random.PositionalRandom;

public final class GroundWater {

    private static final double BLEND_DISTANCE = 25.0;

    private final boolean enabled;
    private final WaterLevelPicker picker;
    private final WaterCellGrid grid;
    private final Density barrier;

    public GroundWater(TerrainSettings settings, ChunkDensity chunk, PositionalRandom random) {
        this.enabled = settings.groundWater();
        this.picker = new WaterLevelPicker(settings, chunk);
        this.grid = new WaterCellGrid(chunk, random, picker);
        this.barrier = chunk.localize(settings.router().barrier());
    }

    public BlockState fill(int x, int y, int z, double density) {
        if (density > 0) {
            return null;
        }
        WaterLevel global = picker.global(y);
        if (!enabled || global.blockAt(y).lava) {
            return global.blockAt(y);
        }
        int gridX = Math.floorDiv(x - 5, WaterCellGrid.WIDTH);
        int gridY = Math.floorDiv(y + 1, WaterCellGrid.TALL);
        int gridZ = Math.floorDiv(z - 5, WaterCellGrid.WIDTH);
        if (grid.onlySea(gridX, gridY, gridZ)) {
            return picker.sea().blockAt(y);
        }
        Nearest nearest = nearestCells(x, y, z, gridX, gridY, gridZ);
        WaterLevel first = grid.level(nearest.firstCell);
        BlockState block = first.blockAt(y);
        double closeness = similarity(nearest.first, nearest.second);
        if (closeness <= 0 || block.water && picker.global(y - 1).blockAt(y - 1).lava) {
            return block;
        }
        Barrier wall = new Barrier(x, y, z);
        WaterLevel second = grid.level(nearest.secondCell);
        if (density + closeness * wall.pressure(first, second) > 0) {
            return null;
        }
        WaterLevel third = grid.level(nearest.thirdCell);
        double firstToThird = similarity(nearest.first, nearest.third);
        if (firstToThird > 0 && density + closeness * firstToThird * wall.pressure(first, third) > 0) {
            return null;
        }
        double secondToThird = similarity(nearest.second, nearest.third);
        if (secondToThird > 0 && density + closeness * secondToThird * wall.pressure(second, third) > 0) {
            return null;
        }
        return block;
    }

    private static double similarity(int first, int second) {
        return 1.0 - Math.abs(second - first) / BLEND_DISTANCE;
    }

    private static final class Nearest {
        int first = Integer.MAX_VALUE;
        int second = Integer.MAX_VALUE;
        int third = Integer.MAX_VALUE;
        int firstCell;
        int secondCell;
        int thirdCell;

        void offer(int cell, int distance) {
            if (first >= distance) {
                thirdCell = secondCell;
                secondCell = firstCell;
                firstCell = cell;
                third = second;
                second = first;
                first = distance;
            } else if (second >= distance) {
                thirdCell = secondCell;
                secondCell = cell;
                third = second;
                second = distance;
            } else if (third >= distance) {
                thirdCell = cell;
                third = distance;
            }
        }
    }

    private Nearest nearestCells(int x, int y, int z, int gridX, int gridY, int gridZ) {
        Nearest nearest = new Nearest();
        for (int dx = 0; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = 0; dz <= 1; dz++) {
                    int cell = grid.cell(gridX + dx, gridY + dy, gridZ + dz);
                    nearest.offer(cell, grid.distanceSquared(cell, x, y, z));
                }
            }
        }
        return nearest;
    }

    private final class Barrier {
        private final int x;
        private final int y;
        private final int z;
        private double noise = Double.NaN;

        Barrier(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        double pressure(WaterLevel a, WaterLevel b) {
            BlockState first = a.blockAt(y);
            BlockState second = b.blockAt(y);
            if (first.lava && second.water || first.water && second.lava) {
                return 2.0;
            }
            int gap = Math.abs(a.surface() - b.surface());
            if (gap == 0) {
                return 0;
            }
            double fromMiddle = y + 0.5 - 0.5 * (a.surface() + b.surface());
            double inside = gap / 2.0 - Math.abs(fromMiddle);
            double slope;
            if (fromMiddle > 0) {
                slope = inside > 0 ? inside / 1.5 : inside / 2.5;
            } else {
                double shifted = 3.0 + inside;
                slope = shifted > 0 ? shifted / 3.0 : shifted / 10.0;
            }
            double wobble = slope < -2 || slope > 2 ? 0 : noise();
            return 2.0 * (wobble + slope);
        }

        private double noise() {
            if (Double.isNaN(noise)) {
                noise = barrier.at(x, y, z);
            }
            return noise;
        }
    }
}

package space.nows.tube.server.world.vanilla.terrain;

import space.nows.tube.server.world.vanilla.density.Density;

final class CellInterpolated implements Density {

    private final Density input;
    private final ChunkDensity chunk;
    private final int cellsAcross;
    private final int cellsUp;
    private double[] corners;

    CellInterpolated(Density input, ChunkDensity chunk) {
        this.input = input;
        this.chunk = chunk;
        this.cellsAcross = 16 / chunk.cellWidth;
        this.cellsUp = chunk.height / chunk.cellHeight;
    }

    @Override
    public double at(int x, int y, int z) {
        if (!chunk.contains(x, y, z)) {
            return input.at(x, y, z);
        }
        if (corners == null) {
            sampleCorners();
        }
        int localX = x - chunk.minX;
        int localY = y - chunk.minY;
        int localZ = z - chunk.minZ;
        int cellX = localX / chunk.cellWidth;
        int cellY = localY / chunk.cellHeight;
        int cellZ = localZ / chunk.cellWidth;
        double fx = (double) (localX - cellX * chunk.cellWidth) / chunk.cellWidth;
        double fy = (double) (localY - cellY * chunk.cellHeight) / chunk.cellHeight;
        double fz = (double) (localZ - cellZ * chunk.cellWidth) / chunk.cellWidth;
        double lowNear = mix(fx, corner(cellX, cellY, cellZ), corner(cellX + 1, cellY, cellZ));
        double highNear = mix(fx, corner(cellX, cellY + 1, cellZ), corner(cellX + 1, cellY + 1, cellZ));
        double lowFar = mix(fx, corner(cellX, cellY, cellZ + 1), corner(cellX + 1, cellY, cellZ + 1));
        double highFar = mix(fx, corner(cellX, cellY + 1, cellZ + 1), corner(cellX + 1, cellY + 1, cellZ + 1));
        double near = mix(fy, lowNear, highNear);
        double far = mix(fy, lowFar, highFar);
        return mix(fz, near, far);
    }

    private void sampleCorners() {
        corners = new double[(cellsAcross + 1) * (cellsAcross + 1) * (cellsUp + 1)];
        for (int cellX = 0; cellX <= cellsAcross; cellX++) {
            for (int cellZ = 0; cellZ <= cellsAcross; cellZ++) {
                for (int cellY = 0; cellY <= cellsUp; cellY++) {
                    corners[slot(cellX, cellY, cellZ)] = input.at(chunk.minX + cellX * chunk.cellWidth,
                            chunk.minY + cellY * chunk.cellHeight, chunk.minZ + cellZ * chunk.cellWidth);
                }
            }
        }
    }

    private double corner(int cellX, int cellY, int cellZ) {
        return corners[slot(cellX, cellY, cellZ)];
    }

    private int slot(int cellX, int cellY, int cellZ) {
        return (cellX * (cellsAcross + 1) + cellZ) * (cellsUp + 1) + cellY;
    }

    private static double mix(double t, double from, double to) {
        return from + t * (to - from);
    }
}

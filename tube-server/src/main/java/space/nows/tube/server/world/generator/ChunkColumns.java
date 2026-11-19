package space.nows.tube.server.world.generator;

import space.nows.tube.api.world.Block;
import space.nows.tube.api.world.generation.ChunkBuilder;

final class ChunkColumns {

    private final Block[][] columns = new Block[256][];
    final int minY;
    final int maxY;

    ChunkColumns(int minY, int maxY) {
        this.minY = minY;
        this.maxY = maxY;
        for (int i = 0; i < columns.length; i++) {
            columns[i] = new Block[maxY - minY];
        }
    }

    boolean inside(int x, int y, int z) {
        return x >= 0 && x < 16 && z >= 0 && z < 16 && y >= minY && y < maxY;
    }

    Block get(int x, int y, int z) {
        return inside(x, y, z) ? columns[x << 4 | z][y - minY] : null;
    }

    boolean isAir(int x, int y, int z) {
        return inside(x, y, z) && columns[x << 4 | z][y - minY] == null;
    }

    void set(int x, int y, int z, Block block) {
        if (inside(x, y, z)) {
            columns[x << 4 | z][y - minY] = block == null || block.isAir() ? null : block;
        }
    }

    void fill(int x, int z, int fromY, int toY, Block block) {
        Block[] column = columns[x << 4 | z];
        for (int y = Math.max(fromY, minY); y < Math.min(toY, maxY); y++) {
            column[y - minY] = block;
        }
    }

    void writeTo(ChunkBuilder chunk) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                Block[] column = columns[x << 4 | z];
                int start = 0;
                while (start < column.length) {
                    Block block = column[start];
                    int end = start + 1;
                    while (end < column.length && column[end] == block) {
                        end++;
                    }
                    if (block != null) {
                        if (end - start == 1) {
                            chunk.setBlock(x, start + minY, z, block);
                        } else {
                            chunk.fillColumn(x, z, start + minY, end + minY, block);
                        }
                    }
                    start = end;
                }
            }
        }
    }
}

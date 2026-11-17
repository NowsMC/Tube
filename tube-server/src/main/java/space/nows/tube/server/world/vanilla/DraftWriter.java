package space.nows.tube.server.world.vanilla;

import space.nows.tube.api.world.ChunkBuilder;
import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.terrain.ChunkDraft;

final class DraftWriter {

    private DraftWriter() {
    }

    static void write(ChunkDraft draft, ChunkBuilder chunk) {
        int fromY = Math.max(draft.minY, chunk.minY());
        int toY = Math.min(draft.maxY(), chunk.maxY());
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                writeColumn(draft, chunk, x, z, fromY, toY);
            }
        }
        writeBiomes(draft, chunk);
    }

    private static void writeColumn(ChunkDraft draft, ChunkBuilder chunk, int x, int z, int fromY, int toY) {
        int start = fromY;
        while (start < toY) {
            BlockState state = draft.get(x, start, z);
            int end = start + 1;
            while (end < toY && draft.get(x, end, z) == state) {
                end++;
            }
            if (!state.air) {
                if (end - start == 1) {
                    chunk.setBlock(x, start, z, state.block);
                } else {
                    chunk.fillColumn(x, z, start, end, state.block);
                }
            }
            start = end;
        }
    }

    private static void writeBiomes(ChunkDraft draft, ChunkBuilder chunk) {
        for (int quartY = 0; quartY < draft.quartHeight(); quartY++) {
            int y = draft.minY + (quartY << 2);
            if (y < chunk.minY() || y >= chunk.maxY()) {
                continue;
            }
            for (int quartZ = 0; quartZ < 4; quartZ++) {
                for (int quartX = 0; quartX < 4; quartX++) {
                    chunk.setBiome(quartX << 2, y, quartZ << 2, draft.quartBiome(quartX, quartY, quartZ));
                }
            }
        }
    }
}

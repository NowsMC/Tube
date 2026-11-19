package space.nows.tube.server.world.generator;

import space.nows.tube.api.world.Block;
import space.nows.tube.api.world.generation.ChunkBuilder;
import space.nows.tube.api.world.generation.WorldGenerator;

public final class VoidGenerator implements WorldGenerator {

    private static final Block STONE = Block.of("stone");

    @Override
    public void generate(ChunkBuilder chunk) {
        if (chunk.chunkX() == 0 && chunk.chunkZ() == 0) {
            chunk.setBlock(0, 63, 0, STONE);
        }
    }

    @Override
    public int spawnHeight(int x, int z) {
        return 64;
    }
}

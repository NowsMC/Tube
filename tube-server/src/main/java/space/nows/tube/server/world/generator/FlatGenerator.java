package space.nows.tube.server.world.generator;

import space.nows.tube.api.world.Block;
import space.nows.tube.api.world.generation.ChunkBuilder;
import space.nows.tube.api.world.generation.WorldGenerator;

public final class FlatGenerator implements WorldGenerator {

    private static final Block GRASS = Block.of("grass_block");

    @Override
    public void generate(ChunkBuilder chunk) {
        chunk.fillLayers(0, 40, GRASS);
    }

    @Override
    public int spawnHeight(int x, int z) {
        return 40;
    }
}

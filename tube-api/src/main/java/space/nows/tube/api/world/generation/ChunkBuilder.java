package space.nows.tube.api.world.generation;

import space.nows.tube.api.world.Block;

public interface ChunkBuilder {

    int chunkX();

    int chunkZ();

    int minY();

    int maxY();

    void setBlock(int x, int y, int z, Block block);

    void fillColumn(int x, int z, int fromY, int toY, Block block);

    void fillLayers(int fromY, int toY, Block block);

    void setBiome(int x, int y, int z, String biome);
}

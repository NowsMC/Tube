package space.nows.tube.api.world;

public interface WorldGenerator {

    void generate(ChunkBuilder chunk);

    default int spawnHeight(int x, int z) {
        return 64;
    }
}

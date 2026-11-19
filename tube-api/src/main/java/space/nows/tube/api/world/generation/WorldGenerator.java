package space.nows.tube.api.world.generation;

public interface WorldGenerator {

    void generate(ChunkBuilder chunk);

    default int spawnHeight(int x, int z) {
        return 64;
    }

    default boolean canSpawnAt(int x, int z) {
        return true;
    }
}

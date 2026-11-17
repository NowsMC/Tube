package space.nows.tube.server.world.vanilla.random;

import java.util.Random;

public final class LcgRandom extends Random implements SeededRandom {

    public LcgRandom(long seed) {
        super(seed);
    }

    public void seedForChunk(long worldSeed, int chunkX, int chunkZ) {
        setSeed(worldSeed);
        long xFactor = nextLong();
        long zFactor = nextLong();
        setSeed(chunkX * xFactor ^ chunkZ * zFactor ^ worldSeed);
    }
}

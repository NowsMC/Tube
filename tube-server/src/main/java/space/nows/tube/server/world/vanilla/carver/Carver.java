package space.nows.tube.server.world.vanilla.carver;

import space.nows.tube.server.world.vanilla.random.LcgRandom;

interface Carver {

    CarverSettings settings();

    void carve(CarveVolume volume, LcgRandom random, int startChunkX, int startChunkZ);
}

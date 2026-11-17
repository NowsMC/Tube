package space.nows.tube.server.world.vanilla.density;

import space.nows.tube.server.world.vanilla.noise.TerrainBlendNoise;

public record TerrainBlend(TerrainBlendNoise noise) implements Density {

    @Override
    public double at(int x, int y, int z) {
        return noise.value(x, y, z);
    }
}

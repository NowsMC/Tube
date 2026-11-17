package space.nows.tube.server.world.vanilla.density;

import space.nows.tube.server.world.vanilla.noise.PairedNoise;

public record NoiseDensity(PairedNoise noise, double xzScale, double yScale) implements Density {

    @Override
    public double at(int x, int y, int z) {
        return noise.value(x * xzScale, y * yScale, z * xzScale);
    }
}

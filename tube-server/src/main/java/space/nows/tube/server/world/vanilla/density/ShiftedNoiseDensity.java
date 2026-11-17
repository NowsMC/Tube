package space.nows.tube.server.world.vanilla.density;

import space.nows.tube.server.world.vanilla.noise.PairedNoise;

import java.util.function.UnaryOperator;

public record ShiftedNoiseDensity(Density shiftX, Density shiftY, Density shiftZ, double xzScale, double yScale,
                                  PairedNoise noise) implements Density {

    @Override
    public double at(int x, int y, int z) {
        return noise.value(x * xzScale + shiftX.at(x, y, z), y * yScale + shiftY.at(x, y, z), z * xzScale + shiftZ.at(x, y, z));
    }

    @Override
    public Density rebuild(UnaryOperator<Density> children) {
        return new ShiftedNoiseDensity(children.apply(shiftX), children.apply(shiftY), children.apply(shiftZ), xzScale, yScale, noise);
    }
}

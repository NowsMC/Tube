package space.nows.tube.server.world.vanilla.density;

import space.nows.tube.server.world.vanilla.noise.PairedNoise;

import java.util.function.UnaryOperator;

public record RarityScaledNoise(Density rarity, PairedNoise noise, Table table) implements Density {

    public enum Table {
        CAVES(new double[]{-0.5, 0, 0.5}, new double[]{0.75, 1.0, 1.5, 2.0}),
        TUNNELS(new double[]{-0.75, -0.5, 0.5, 0.75}, new double[]{0.5, 0.75, 1.0, 2.0, 3.0});

        private final double[] bounds;
        private final double[] scales;

        Table(double[] bounds, double[] scales) {
            this.bounds = bounds;
            this.scales = scales;
        }

        double scale(double value) {
            int index = 0;
            while (index < bounds.length && value >= bounds[index]) {
                index++;
            }
            return scales[index];
        }
    }

    @Override
    public double at(int x, int y, int z) {
        double scale = table.scale(rarity.at(x, y, z));
        return scale * Math.abs(noise.value(x / scale, y / scale, z / scale));
    }

    @Override
    public Density rebuild(UnaryOperator<Density> children) {
        return new RarityScaledNoise(children.apply(rarity), noise, table);
    }
}

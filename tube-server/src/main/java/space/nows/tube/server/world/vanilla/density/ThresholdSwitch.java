package space.nows.tube.server.world.vanilla.density;

import java.util.function.UnaryOperator;

public record ThresholdSwitch(Density input, double[] thresholds, Density[] branches) implements Density {

    @Override
    public double at(int x, int y, int z) {
        double value = input.at(x, y, z);
        int branch = 0;
        while (branch < thresholds.length && value >= thresholds[branch]) {
            branch++;
        }
        return branches[branch].at(x, y, z);
    }

    @Override
    public Density rebuild(UnaryOperator<Density> children) {
        Density[] rebuilt = new Density[branches.length];
        for (int i = 0; i < branches.length; i++) {
            rebuilt[i] = children.apply(branches[i]);
        }
        return new ThresholdSwitch(children.apply(input), thresholds, rebuilt);
    }
}

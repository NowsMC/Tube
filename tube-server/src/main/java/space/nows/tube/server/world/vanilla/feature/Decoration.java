package space.nows.tube.server.world.vanilla.feature;

import space.nows.tube.server.world.vanilla.feature.placement.PlacementStep;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.List;

public record Decoration(Feature feature, List<PlacementStep> steps) implements Feature {

    public static final Decoration EMPTY = new Decoration(Feature.NONE, List.of());

    @Override
    public boolean place(DecorationPass pass, Xoroshiro random, int x, int y, int z) {
        boolean[] placed = new boolean[1];
        walk(pass, random, 0, x, y, z, false, placed);
        return placed[0];
    }

    void decorate(DecorationPass pass, Xoroshiro random, int x, int y, int z) {
        walk(pass, random, 0, x, y, z, true, new boolean[1]);
    }

    private void walk(DecorationPass pass, Xoroshiro random, int step, int x, int y, int z, boolean topLevel, boolean[] placed) {
        if (step < steps.size()) {
            steps.get(step).apply(pass, random, x, y, z, (nx, ny, nz) -> walk(pass, random, step + 1, nx, ny, nz, topLevel, placed));
            return;
        }
        if (!topLevel) {
            placed[0] |= feature.place(pass, random, x, y, z);
            return;
        }
        long seed = random.nextLong();
        if (pass.reaches(x, z)) {
            placed[0] |= feature.place(pass, new Xoroshiro(seed), x, y, z);
        }
    }
}

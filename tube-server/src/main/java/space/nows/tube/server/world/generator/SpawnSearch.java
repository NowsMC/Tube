package space.nows.tube.server.world.generator;

import space.nows.tube.api.world.WorldGenerator;

public final class SpawnSearch {

    private SpawnSearch() {
    }

    public static int[] find(WorldGenerator generator) {
        for (int radius = 0; radius <= 1024; radius += 16) {
            int steps = Math.max(1, radius / 2);
            for (int step = 0; step < steps; step++) {
                double angle = step * Math.PI * 2 / steps;
                int x = (int) Math.round(Math.cos(angle) * radius);
                int z = (int) Math.round(Math.sin(angle) * radius);
                if (generator.canSpawnAt(x, z)) {
                    return new int[]{x, z};
                }
            }
        }
        return new int[]{0, 0};
    }
}

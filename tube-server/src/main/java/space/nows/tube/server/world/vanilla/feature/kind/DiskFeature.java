package space.nows.tube.server.world.vanilla.feature.kind;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.feature.DecorationArea;
import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.feature.Feature;
import space.nows.tube.server.world.vanilla.feature.choice.BlockChoice;
import space.nows.tube.server.world.vanilla.feature.sample.IntSampler;
import space.nows.tube.server.world.vanilla.feature.test.BlockTest;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

public record DiskFeature(BlockChoice block, BlockTest replaces, IntSampler radius, int halfHeight) implements Feature {

    public static DiskFeature read(JsonObject config) {
        return new DiskFeature(BlockChoice.read(config.get("state_provider")), BlockTest.read(config.get("target")),
                IntSampler.read(config.get("radius")), config.get("half_height").getAsInt());
    }

    @Override
    public boolean place(DecorationPass pass, Xoroshiro random, int x, int y, int z) {
        DecorationArea area = pass.area;
        int size = radius.sample(random);
        boolean placed = false;
        for (int dx = -size; dx <= size; dx++) {
            for (int dz = -size; dz <= size; dz++) {
                if (dx * dx + dz * dz <= size * size) {
                    placed |= fillColumn(area, random, x + dx, y, z + dz);
                }
            }
        }
        return placed;
    }

    private boolean fillColumn(DecorationArea area, Xoroshiro random, int x, int y, int z) {
        boolean placed = false;
        for (int columnY = y + halfHeight; columnY > y - halfHeight - 1; columnY--) {
            if (!replaces.test(area, x, columnY, z)) {
                continue;
            }
            BlockState state = block.choose(random, area, x, columnY, z);
            if (state != null) {
                area.set(x, columnY, z, state);
                placed = true;
            }
        }
        return placed;
    }
}

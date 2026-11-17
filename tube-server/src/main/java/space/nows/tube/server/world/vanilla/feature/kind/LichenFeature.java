package space.nows.tube.server.world.vanilla.feature.kind;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.block.BlockStates;
import space.nows.tube.server.world.vanilla.feature.DecorationArea;
import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.feature.Feature;
import space.nows.tube.server.world.vanilla.feature.test.BlockNames;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.Set;

public record LichenFeature(BlockState block, Set<String> growsOn, boolean ceiling, boolean floor, boolean walls, int searchRange)
        implements Feature {

    private static final int[][] SEARCH_DIRECTIONS = {{0, 1, 0}, {0, -1, 0}, {0, 0, -1}, {1, 0, 0}, {0, 0, 1}, {-1, 0, 0}};

    public static LichenFeature read(JsonObject config) {
        return new LichenFeature(
                BlockStates.of(config.get("block").getAsString()),
                BlockNames.read(config.get("can_be_placed_on")),
                !config.has("can_place_on_ceiling") || config.get("can_place_on_ceiling").getAsBoolean(),
                config.has("can_place_on_floor") && config.get("can_place_on_floor").getAsBoolean(),
                !config.has("can_place_on_wall") || config.get("can_place_on_wall").getAsBoolean(),
                config.has("search_range") ? config.get("search_range").getAsInt() : 10);
    }

    @Override
    public boolean place(DecorationPass pass, Xoroshiro random, int x, int y, int z) {
        DecorationArea area = pass.area;
        if (!area.isAir(x, y, z)) {
            return false;
        }
        int first = random.nextInt(SEARCH_DIRECTIONS.length);
        for (int i = 0; i < SEARCH_DIRECTIONS.length; i++) {
            int[] direction = SEARCH_DIRECTIONS[(first + i) % SEARCH_DIRECTIONS.length];
            for (int step = 0; step < searchRange; step++) {
                int px = x + direction[0] * step;
                int py = y + direction[1] * step;
                int pz = z + direction[2] * step;
                if (!area.isAir(px, py, pz)) {
                    break;
                }
                BlockState attached = attachTo(area, px, py, pz);
                if (attached != null) {
                    area.set(px, py, pz, attached);
                    return true;
                }
            }
        }
        return false;
    }

    private BlockState attachTo(DecorationArea area, int x, int y, int z) {
        if (ceiling && growsOn.contains(area.get(x, y + 1, z).key)) {
            return block.with("up", "true");
        }
        if (floor && growsOn.contains(area.get(x, y - 1, z).key)) {
            return block.with("down", "true");
        }
        if (walls) {
            for (WallFace face : WallFace.values()) {
                if (growsOn.contains(area.get(x + face.dx, y, z + face.dz).key)) {
                    return block.with(face.property, "true");
                }
            }
        }
        return null;
    }
}

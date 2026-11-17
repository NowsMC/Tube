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

public record SpringFeature(BlockState fluid, boolean needsFloor, int walls, int openings, Set<String> rock) implements Feature {

    private static final int[][] SIDES_AND_BELOW = {{-1, 0, 0}, {1, 0, 0}, {0, 0, -1}, {0, 0, 1}, {0, -1, 0}};

    public static SpringFeature read(JsonObject config) {
        return new SpringFeature(
                BlockStates.of(config.getAsJsonObject("state").get("Name").getAsString()),
                !config.has("requires_block_below") || config.get("requires_block_below").getAsBoolean(),
                config.has("rock_count") ? config.get("rock_count").getAsInt() : 4,
                config.has("hole_count") ? config.get("hole_count").getAsInt() : 1,
                BlockNames.read(config.get("valid_blocks")));
    }

    @Override
    public boolean place(DecorationPass pass, Xoroshiro random, int x, int y, int z) {
        DecorationArea area = pass.area;
        if (!isRock(area, x, y + 1, z) || needsFloor && !isRock(area, x, y - 1, z)) {
            return false;
        }
        BlockState current = area.get(x, y, z);
        if (!current.air && !rock.contains(current.key)) {
            return false;
        }
        int rockCount = 0;
        int holeCount = 0;
        for (int[] side : SIDES_AND_BELOW) {
            if (isRock(area, x + side[0], y + side[1], z + side[2])) {
                rockCount++;
            }
            if (area.isAir(x + side[0], y + side[1], z + side[2])) {
                holeCount++;
            }
        }
        if (rockCount != walls || holeCount != openings) {
            return false;
        }
        area.set(x, y, z, fluid);
        return true;
    }

    private boolean isRock(DecorationArea area, int x, int y, int z) {
        return rock.contains(area.get(x, y, z).key);
    }
}

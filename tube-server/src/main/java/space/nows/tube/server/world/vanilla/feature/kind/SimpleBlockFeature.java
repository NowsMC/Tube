package space.nows.tube.server.world.vanilla.feature.kind;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.feature.DecorationArea;
import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.feature.Feature;
import space.nows.tube.server.world.vanilla.feature.choice.BlockChoice;
import space.nows.tube.server.world.vanilla.feature.test.PlantSupport;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

public record SimpleBlockFeature(BlockChoice block) implements Feature {

    public static SimpleBlockFeature read(JsonObject config) {
        return new SimpleBlockFeature(BlockChoice.read(config.get("to_place")));
    }

    @Override
    public boolean place(DecorationPass pass, Xoroshiro random, int x, int y, int z) {
        DecorationArea area = pass.area;
        BlockState state = block.choose(random, area, x, y, z);
        if (state == null || !PlantSupport.canStay(area, state, x, y, z)) {
            return false;
        }
        if ("lower".equals(state.property("half"))) {
            return placeTall(area, state, x, y, z);
        }
        if (area.get(x, y, z).water && state.hasProperty("waterlogged")) {
            state = state.with("waterlogged", "true");
        }
        area.set(x, y, z, state);
        return true;
    }

    private static boolean placeTall(DecorationArea area, BlockState lower, int x, int y, int z) {
        if (!area.isAir(x, y + 1, z)) {
            return false;
        }
        area.set(x, y, z, lower);
        area.set(x, y + 1, z, lower.with("half", "upper"));
        return true;
    }
}

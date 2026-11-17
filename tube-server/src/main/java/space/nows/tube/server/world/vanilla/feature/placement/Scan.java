package space.nows.tube.server.world.vanilla.feature.placement;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.feature.DecorationArea;
import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.feature.test.BlockTest;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

record Scan(int step, int maxSteps, BlockTest target, BlockTest through) implements PlacementStep {

    static Scan read(JsonObject json) {
        return new Scan(json.get("direction_of_search").getAsString().equals("up") ? 1 : -1,
                json.get("max_steps").getAsInt(),
                BlockTest.read(json.get("target_condition")),
                json.has("allowed_search_condition") ? BlockTest.read(json.get("allowed_search_condition")) : BlockTest.ANY);
    }

    @Override
    public void apply(DecorationPass pass, Xoroshiro random, int x, int y, int z, Next next) {
        DecorationArea area = pass.area;
        if (!through.test(area, x, y, z)) {
            return;
        }
        int scanY = y;
        for (int i = 0; i < maxSteps; i++) {
            if (target.test(area, x, scanY, z)) {
                next.at(x, scanY, z);
                return;
            }
            scanY += step;
            if (area.outsideHeight(scanY)) {
                return;
            }
            if (!through.test(area, x, scanY, z)) {
                break;
            }
        }
        if (target.test(area, x, scanY, z)) {
            next.at(x, scanY, z);
        }
    }
}

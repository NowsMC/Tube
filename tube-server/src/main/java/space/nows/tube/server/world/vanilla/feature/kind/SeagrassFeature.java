package space.nows.tube.server.world.vanilla.feature.kind;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.block.BlockStates;
import space.nows.tube.server.world.vanilla.block.Heightmap;
import space.nows.tube.server.world.vanilla.feature.DecorationArea;
import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.feature.Feature;
import space.nows.tube.server.world.vanilla.feature.test.PlantSupport;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

public record SeagrassFeature(float tallChance) implements Feature {

    private static final BlockState SHORT = BlockStates.of("seagrass");
    private static final BlockState TALL = BlockStates.of("tall_seagrass");

    public static SeagrassFeature read(JsonObject config) {
        return new SeagrassFeature(config.get("probability").getAsFloat());
    }

    @Override
    public boolean place(DecorationPass pass, Xoroshiro random, int x, int y, int z) {
        DecorationArea area = pass.area;
        int px = x + random.nextInt(8) - random.nextInt(8);
        int pz = z + random.nextInt(8) - random.nextInt(8);
        int py = area.height(Heightmap.SOLID, px, pz);
        if (!area.get(px, py, pz).isBlock("minecraft:water")) {
            return false;
        }
        boolean tall = random.nextDouble() < tallChance;
        if (!PlantSupport.canStay(area, tall ? TALL : SHORT, px, py, pz)) {
            return false;
        }
        if (!tall) {
            area.set(px, py, pz, SHORT);
            return true;
        }
        if (!area.get(px, py + 1, pz).isBlock("minecraft:water")) {
            return false;
        }
        area.set(px, py, pz, TALL.with("half", "lower"));
        area.set(px, py + 1, pz, TALL.with("half", "upper"));
        return true;
    }
}

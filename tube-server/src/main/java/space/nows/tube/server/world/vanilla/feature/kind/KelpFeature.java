package space.nows.tube.server.world.vanilla.feature.kind;

import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.block.BlockStates;
import space.nows.tube.server.world.vanilla.block.Heightmap;
import space.nows.tube.server.world.vanilla.feature.DecorationArea;
import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.feature.Feature;
import space.nows.tube.server.world.vanilla.feature.test.PlantSupport;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

public record KelpFeature() implements Feature {

    private static final BlockState TIP = BlockStates.of("kelp");
    private static final BlockState STEM = BlockStates.of("kelp_plant");

    @Override
    public boolean place(DecorationPass pass, Xoroshiro random, int x, int y, int z) {
        DecorationArea area = pass.area;
        int py = area.height(Heightmap.SOLID, x, z);
        if (!isWater(area, x, py, z)) {
            return false;
        }
        int length = 1 + random.nextInt(10);
        for (int i = 0; i <= length; i++, py++) {
            if (isWater(area, x, py, z) && isWater(area, x, py + 1, z) && PlantSupport.canStay(area, STEM, x, py, z)) {
                area.set(x, py, z, i == length ? tip(random) : STEM);
                continue;
            }
            if (i > 0) {
                if (PlantSupport.canStay(area, TIP, x, py - 1, z) && !area.get(x, py - 2, z).isBlock("minecraft:kelp")) {
                    area.set(x, py - 1, z, tip(random));
                }
                break;
            }
        }
        return true;
    }

    private static boolean isWater(DecorationArea area, int x, int y, int z) {
        return area.get(x, y, z).isBlock("minecraft:water");
    }

    private static BlockState tip(Xoroshiro random) {
        return TIP.with("age", Integer.toString(random.nextInt(4) + 20));
    }
}

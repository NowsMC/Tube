package space.nows.tube.server.world.vanilla.feature.tree.decorator;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.block.Heightmap;
import space.nows.tube.server.world.vanilla.feature.DecorationArea;
import space.nows.tube.server.world.vanilla.feature.choice.BlockChoice;
import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;

record GroundLitter(BlockChoice litter, int tries, int radius, int height) implements TreeDecorator {

    static GroundLitter read(JsonObject json) {
        return new GroundLitter(BlockChoice.read(json.get("block_state_provider")),
                json.has("tries") ? json.get("tries").getAsInt() : 128,
                json.has("radius") ? json.get("radius").getAsInt() : 2,
                json.has("height") ? json.get("height").getAsInt() : 1);
    }

    @Override
    public void decorate(TreeGrowth growth) {
        if (growth.logs().isEmpty()) {
            return;
        }
        int[] base = growth.logs().getFirst();
        DecorationArea area = growth.area;
        for (int i = 0; i < tries; i++) {
            int x = base[0] + growth.random.nextBetween(-radius, radius);
            int y = base[1] + growth.random.nextBetween(-height, height);
            int z = base[2] + growth.random.nextBetween(-radius, radius);
            if (canCover(area, x, y, z)) {
                BlockState state = litter.choose(growth.random, area, x, y + 1, z);
                if (state != null) {
                    area.set(x, y + 1, z, state);
                }
            }
        }
    }

    private static boolean canCover(DecorationArea area, int x, int y, int z) {
        BlockState ground = area.get(x, y, z);
        return area.isAir(x, y + 1, z) && ground.blocksMotion && !ground.leaves
                && area.height(Heightmap.SOLID_OR_FLUID_NOT_LEAVES, x, z) <= y + 1;
    }
}

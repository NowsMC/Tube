package space.nows.tube.server.world.vanilla.feature.placement;

import space.nows.tube.server.world.vanilla.block.Heightmap;
import space.nows.tube.server.world.vanilla.feature.DecorationArea;
import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

record ShallowWater(int maxDepth) implements PlacementStep {

    @Override
    public void apply(DecorationPass pass, Xoroshiro random, int x, int y, int z, Next next) {
        DecorationArea area = pass.area;
        if (area.height(Heightmap.ANY_BLOCK, x, z) - area.height(Heightmap.SOLID, x, z) <= maxDepth) {
            next.at(x, y, z);
        }
    }
}

package space.nows.tube.server.world.vanilla.feature.placement;

import space.nows.tube.server.world.vanilla.block.Heightmap;
import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

record OnHeightmap(Heightmap heightmap) implements PlacementStep {

    @Override
    public void apply(DecorationPass pass, Xoroshiro random, int x, int y, int z, Next next) {
        int top = pass.area.height(heightmap, x, z);
        if (top > pass.area.minY) {
            next.at(x, top, z);
        }
    }
}

package space.nows.tube.server.world.vanilla.feature.placement;

import space.nows.tube.server.world.vanilla.biome.BiomeInfo;
import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

record BiomeFilter() implements PlacementStep {

    @Override
    public void apply(DecorationPass pass, Xoroshiro random, int x, int y, int z, Next next) {
        BiomeInfo biome = pass.area.biome(x, y, z);
        if (biome != null && biome.features().contains(pass.current())) {
            next.at(x, y, z);
        }
    }
}

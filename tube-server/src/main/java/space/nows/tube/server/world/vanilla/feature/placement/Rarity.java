package space.nows.tube.server.world.vanilla.feature.placement;

import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

record Rarity(float chance) implements PlacementStep {

    @Override
    public void apply(DecorationPass pass, Xoroshiro random, int x, int y, int z, Next next) {
        if (random.nextFloat() < chance) {
            next.at(x, y, z);
        }
    }
}

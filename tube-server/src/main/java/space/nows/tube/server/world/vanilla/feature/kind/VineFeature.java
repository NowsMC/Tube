package space.nows.tube.server.world.vanilla.feature.kind;

import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.block.BlockStates;
import space.nows.tube.server.world.vanilla.feature.DecorationArea;
import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.feature.Feature;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

public record VineFeature() implements Feature {

    private static final BlockState VINE = BlockStates.of("vine");

    @Override
    public boolean place(DecorationPass pass, Xoroshiro random, int x, int y, int z) {
        DecorationArea area = pass.area;
        if (!area.isAir(x, y, z)) {
            return false;
        }
        if (area.get(x, y + 1, z).blocksMotion) {
            area.set(x, y, z, VINE.with("up", "true"));
            return true;
        }
        for (WallFace face : WallFace.values()) {
            if (area.get(x + face.dx, y, z + face.dz).blocksMotion) {
                area.set(x, y, z, VINE.with(face.property, "true"));
                return true;
            }
        }
        return false;
    }
}

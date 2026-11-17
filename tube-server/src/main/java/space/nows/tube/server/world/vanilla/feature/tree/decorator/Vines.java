package space.nows.tube.server.world.vanilla.feature.tree.decorator;

import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.block.BlockStates;
import space.nows.tube.server.world.vanilla.feature.DecorationArea;

final class Vines {

    static final int[][] SIDES = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
    private static final String[] FACING_BACK = {"south", "west", "north", "east"};
    private static final BlockState VINE = BlockStates.of("vine");

    private Vines() {
    }

    static void hang(DecorationArea area, int x, int y, int z, int side) {
        if (area.isAir(x, y, z)) {
            area.set(x, y, z, VINE.with(FACING_BACK[side], "true"));
        }
    }
}

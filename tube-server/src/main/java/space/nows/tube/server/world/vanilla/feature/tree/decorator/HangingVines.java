package space.nows.tube.server.world.vanilla.feature.tree.decorator;

import space.nows.tube.server.world.vanilla.feature.DecorationArea;
import space.nows.tube.server.world.vanilla.feature.tree.Packed;
import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;

import java.util.List;

record HangingVines(float chance) implements TreeDecorator {

    private static final int MAX_LENGTH = 5;

    @Override
    public void decorate(TreeGrowth growth) {
        DecorationArea area = growth.area;
        for (long leaf : List.copyOf(growth.leafPositions())) {
            for (int side = 0; side < Vines.SIDES.length; side++) {
                if (growth.random.nextFloat() >= chance) {
                    continue;
                }
                int x = Packed.x(leaf) + Vines.SIDES[side][0];
                int z = Packed.z(leaf) + Vines.SIDES[side][1];
                int y = Packed.y(leaf);
                for (int length = 0; length < MAX_LENGTH && area.isAir(x, y, z); length++, y--) {
                    Vines.hang(area, x, y, z, side);
                }
            }
        }
    }
}

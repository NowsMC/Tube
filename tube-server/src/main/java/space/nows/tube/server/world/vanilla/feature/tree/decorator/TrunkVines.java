package space.nows.tube.server.world.vanilla.feature.tree.decorator;

import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;

final class TrunkVines implements TreeDecorator {

    @Override
    public void decorate(TreeGrowth growth) {
        for (int[] log : growth.logs()) {
            for (int side = 0; side < Vines.SIDES.length; side++) {
                if (growth.random.nextInt(3) > 0) {
                    Vines.hang(growth.area, log[0] + Vines.SIDES[side][0], log[1], log[2] + Vines.SIDES[side][1], side);
                }
            }
        }
    }
}

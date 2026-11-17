package space.nows.tube.server.world.vanilla.feature.tree.decorator;

import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.block.BlockStates;
import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;

record CocoaPods(float chance) implements TreeDecorator {

    private static final BlockState COCOA = BlockStates.of("cocoa");
    private static final String[] FACING = {"north", "east", "south", "west"};
    private static final int LOWEST_LOGS = 2;

    @Override
    public void decorate(TreeGrowth growth) {
        if (growth.random.nextFloat() >= chance || growth.logs().isEmpty()) {
            return;
        }
        int bottom = growth.logs().getFirst()[1];
        for (int[] log : growth.logs()) {
            if (log[1] - bottom > LOWEST_LOGS) {
                continue;
            }
            for (int side = 0; side < Vines.SIDES.length; side++) {
                if (growth.random.nextFloat() > 0.25F) {
                    continue;
                }
                int x = log[0] - Vines.SIDES[side][0];
                int z = log[2] - Vines.SIDES[side][1];
                if (growth.area.isAir(x, log[1], z)) {
                    BlockState pod = COCOA.with("age", Integer.toString(growth.random.nextInt(3))).with("facing", FACING[side]);
                    growth.area.set(x, log[1], z, pod);
                }
            }
        }
    }
}

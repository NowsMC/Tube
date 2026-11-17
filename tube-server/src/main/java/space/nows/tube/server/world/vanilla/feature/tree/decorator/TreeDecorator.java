package space.nows.tube.server.world.vanilla.feature.tree.decorator;

import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;

@FunctionalInterface
public interface TreeDecorator {

    void decorate(TreeGrowth growth);
}

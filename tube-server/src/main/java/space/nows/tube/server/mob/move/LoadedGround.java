package space.nows.tube.server.mob.move;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.pathfinding.PNode;
import net.minestom.server.entity.pathfinding.generators.GroundNodeGenerator;
import net.minestom.server.entity.pathfinding.generators.NodeGenerator;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.Collection;
import java.util.OptionalDouble;
import java.util.Set;

final class LoadedGround implements NodeGenerator {

    private final GroundNodeGenerator ground = new GroundNodeGenerator();

    @Override
    public Collection<? extends PNode> getWalkable(Block.Getter getter, Set<PNode> visited, PNode current, Point goal, BoundingBox box) {
        return ground.getWalkable(walled(getter), visited, current, goal, box);
    }

    @Override
    public boolean hasGravitySnap() {
        return ground.hasGravitySnap();
    }

    @Override
    public OptionalDouble gravitySnap(Block.Getter getter, double x, double y, double z, BoundingBox box, double maxFall) {
        return ground.gravitySnap(walled(getter), x, y, z, box, maxFall);
    }

    @Override
    public boolean canMoveTowards(Block.Getter getter, Point start, Point end, BoundingBox box) {
        return ground.canMoveTowards(walled(getter), start, end, box);
    }

    @Override
    public boolean pointInvalid(Block.Getter getter, Point point, BoundingBox box) {
        return ground.pointInvalid(walled(getter), point, box);
    }

    @Override
    public double heuristic(Point from, Point to) {
        return ground.heuristic(from, to);
    }

    private static Block.Getter walled(Block.Getter getter) {
        if (!(getter instanceof Instance instance)) {
            return getter;
        }
        return (x, y, z, condition) -> instance.isChunkLoaded(x >> 4, z >> 4) ? instance.getBlock(x, y, z, condition) : Block.BARRIER;
    }
}

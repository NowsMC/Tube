package space.nows.tube.server.mob.kind.monster.silverfish;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.api.world.GameRule;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.world.rule.RuleBook;

final class MergeWithStone extends Behavior {

    private static final int CHANCE = 10;
    private static final Vec[] SIDES = {new Vec(1, 0, 0), new Vec(-1, 0, 0), new Vec(0, 1, 0), new Vec(0, -1, 0), new Vec(0, 0, 1), new Vec(0, 0, -1)};

    private final Creature silverfish;
    private final Wander stroll;
    private Point host;

    MergeWithStone(Creature silverfish) {
        super(Control.MOVE);
        this.silverfish = silverfish;
        this.stroll = Wander.anywhere(silverfish, 1.0, CHANCE);
    }

    @Override
    public boolean canStart() {
        host = null;
        if (silverfish.target() != null || !silverfish.navigation().isDone()) {
            return false;
        }
        Instance instance = silverfish.getInstance();
        if (RuleBook.enabled(instance, GameRule.MOB_GRIEFING) && silverfish.random().nextInt(halved(CHANCE)) == 0) {
            Point at = silverfish.getPosition().add(0, 0.5, 0).add(SIDES[silverfish.random().nextInt(SIDES.length)]);
            if (Infestation.canHost(instance.getBlock(at))) {
                host = at;
                return true;
            }
        }
        return stroll.canStart();
    }

    @Override
    public boolean canContinue() {
        return host == null && stroll.canContinue();
    }

    @Override
    public void start() {
        if (host == null) {
            stroll.start();
            return;
        }
        Instance instance = silverfish.getInstance();
        Block block = instance.getBlock(host);
        if (Infestation.canHost(block)) {
            instance.setBlock(host, Infestation.infest(block));
            silverfish.remove();
        }
    }

    @Override
    public void stop() {
        if (host == null) {
            stroll.stop();
        }
    }
}

package space.nows.tube.server.mob.kind.monster.silverfish;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import space.nows.tube.api.entity.mob.brain.Behavior;
import space.nows.tube.api.world.GameRule;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.world.rule.RuleBook;

final class WakeFriends extends Behavior {

    private static final int DELAY = 20;
    private static final int REACH = 10;
    private static final int HEIGHT = 5;

    private final Creature silverfish;
    private int countdown;

    WakeFriends(Creature silverfish) {
        this.silverfish = silverfish;
    }

    void alarm() {
        if (countdown == 0) {
            countdown = halved(DELAY);
        }
    }

    @Override
    public boolean canStart() {
        return countdown > 0;
    }

    @Override
    public void tick() {
        if (--countdown > 0) {
            return;
        }
        Instance instance = silverfish.getInstance();
        boolean griefing = RuleBook.enabled(instance, GameRule.MOB_GRIEFING);
        Point base = new Vec(silverfish.getPosition().blockX(), silverfish.getPosition().blockY(), silverfish.getPosition().blockZ());
        for (int dy : outward(HEIGHT)) {
            for (int dx : outward(REACH)) {
                for (int dz : outward(REACH)) {
                    Point at = base.add(dx, dy, dz);
                    Block block = instance.getBlock(at);
                    if (!Infestation.infested(block)) {
                        continue;
                    }
                    if (griefing) {
                        instance.setBlock(at, Block.AIR);
                        Infestation.release(instance, at);
                    } else {
                        instance.setBlock(at, Infestation.restore(block));
                    }
                    if (silverfish.random().nextBoolean()) {
                        return;
                    }
                }
            }
        }
    }

    private static int[] outward(int reach) {
        int[] steps = new int[reach * 2 + 1];
        for (int i = 1; i < steps.length; i++) {
            steps[i] = i % 2 == 1 ? (i + 1) / 2 : -(i / 2);
        }
        return steps;
    }
}

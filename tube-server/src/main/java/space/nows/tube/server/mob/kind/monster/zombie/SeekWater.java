package space.nows.tube.server.mob.kind.monster.zombie;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.move.Liquids;
import space.nows.tube.server.mob.sense.Daylight;

final class SeekWater extends Behavior {

    private static final int ATTEMPTS = 10;
    private static final int RANGE = 10;
    private static final int DEPTH = 2;

    private final Creature drowned;
    private Point water;

    SeekWater(Creature drowned) {
        super(Control.MOVE);
        this.drowned = drowned;
    }

    @Override
    public boolean canStart() {
        if (!Daylight.brightOutside(drowned.getInstance()) || drowned.inWater()) {
            return false;
        }
        water = find();
        return water != null;
    }

    @Override
    public boolean canContinue() {
        return !drowned.navigation().isDone();
    }

    @Override
    public void start() {
        drowned.navigation().moveTo(water, 1.0);
    }

    private Point find() {
        for (int i = 0; i < ATTEMPTS; i++) {
            Point at = new Vec(drowned.getPosition().blockX() + drowned.random().nextInt(RANGE * 2) - RANGE,
                    drowned.getPosition().blockY() + drowned.random().nextInt(DEPTH * 3) - DEPTH,
                    drowned.getPosition().blockZ() + drowned.random().nextInt(RANGE * 2) - RANGE);
            if (Liquids.isWater(drowned.blockAt(at))) {
                return at.add(0.5, 0, 0.5);
            }
        }
        return null;
    }
}

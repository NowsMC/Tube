package space.nows.tube.server.mob.kind.animal.horse;

import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.server.mob.Creature;

final class Rear extends Behavior {

    private static final int STAND_INTERVAL = 80;

    private final Creature horse;
    private final Rearing rearing;
    private int nextStand = -STAND_INTERVAL;

    Rear(Creature horse, Rearing rearing) {
        this.horse = horse;
        this.rearing = rearing;
    }

    @Override
    public boolean canStart() {
        nextStand++;
        if (nextStand > 0 && horse.random().nextInt(1000) < nextStand) {
            nextStand = -STAND_INTERVAL;
            return horse.random().nextInt(10) == 0;
        }
        return false;
    }

    @Override
    public boolean canContinue() {
        return false;
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void start() {
        rearing.begin();
    }
}

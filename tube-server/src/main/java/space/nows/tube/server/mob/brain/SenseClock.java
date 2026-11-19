package space.nows.tube.server.mob.brain;

import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.api.entity.mob.brain.Sense;

final class SenseClock {

    private final Sense sense;
    private int countdown;

    SenseClock(Sense sense, int offset) {
        this.sense = sense;
        this.countdown = Math.floorMod(offset, interval());
    }

    void tick(Brain brain) {
        if (--countdown <= 0) {
            countdown = interval();
            sense.sense(brain);
        }
    }

    private int interval() {
        return Math.max(1, sense.interval());
    }
}

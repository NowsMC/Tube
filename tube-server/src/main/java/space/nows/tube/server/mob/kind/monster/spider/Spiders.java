package space.nows.tube.server.mob.kind.monster.spider;

import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.sense.Daylight;

final class Spiders {

    private static final float DARK_BELOW = 0.5f;

    private Spiders() {
    }

    static boolean dark(Creature spider) {
        return Daylight.brightness(spider.getInstance(), spider.getPosition()) < DARK_BELOW;
    }
}

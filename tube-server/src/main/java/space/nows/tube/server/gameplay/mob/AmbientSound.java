package space.nows.tube.server.gameplay.mob;

import java.util.concurrent.ThreadLocalRandom;

final class AmbientSound {

    private static final int CHANCE = 240;

    private AmbientSound() {
    }

    static void tick(Mob mob) {
        if (mob.kind().ambient && !mob.isDead() && ThreadLocalRandom.current().nextInt(CHANCE) == 0) {
            MobVoice.play(mob, "ambient");
        }
    }
}

package space.nows.tube.server.mob.sound;

import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.trait.Trait;

public final class AmbientSound implements Trait {

    private static final int ROLL = 1000;

    private final Creature creature;
    private int timer;

    public AmbientSound(Creature creature) {
        this.creature = creature;
        this.timer = -creature.kind().ambientInterval();
    }

    @Override
    public void tick() {
        if (creature.isDead()) {
            return;
        }
        if (creature.random().nextInt(ROLL) < timer++) {
            timer = -creature.kind().ambientInterval();
            MobVoice.play(creature, "ambient");
        }
    }
}

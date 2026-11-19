package space.nows.tube.server.mob.kind.animal.horse;

import net.minestom.server.entity.metadata.animal.AbstractHorseMeta;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.sound.MobVoice;
import space.nows.tube.server.mob.trait.Trait;

final class Rearing implements Trait {

    private static final int DURATION = 20;

    private final Creature horse;
    private int left;

    Rearing(Creature horse) {
        this.horse = horse;
    }

    void begin() {
        if (horse.getEntityMeta() instanceof AbstractHorseMeta meta) {
            meta.setRearing(true);
            left = DURATION;
            MobVoice.play(horse, "angry");
        }
    }

    @Override
    public void tick() {
        if (left > 0 && --left == 0 && horse.getEntityMeta() instanceof AbstractHorseMeta meta) {
            meta.setRearing(false);
        }
    }
}

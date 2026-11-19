package space.nows.tube.server.mob.kind.fish;

import net.minestom.server.entity.Entity;
import space.nows.tube.api.entity.mob.brain.Behavior;
import space.nows.tube.server.mob.Creature;

final class Puff extends Behavior {

    private static final double ALARM_RANGE = 2;

    private final Creature fish;
    private final Puffing puffing;

    Puff(Creature fish, Puffing puffing) {
        this.fish = fish;
        this.puffing = puffing;
    }

    @Override
    public boolean canStart() {
        for (Entity entity : fish.getInstance().getNearbyEntities(fish.getPosition(), ALARM_RANGE + fish.getBoundingBox().width())) {
            if (entity != fish && Puffing.scary(entity)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        puffing.inflate();
    }

    @Override
    public void stop() {
        puffing.calm();
    }
}

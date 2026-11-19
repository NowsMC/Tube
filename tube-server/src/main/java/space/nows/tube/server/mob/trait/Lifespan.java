package space.nows.tube.server.mob.trait;

import space.nows.tube.server.mob.Creature;

public final class Lifespan implements Trait {

    private final Creature creature;
    private final int limit;
    private int lived;

    public Lifespan(Creature creature, int limit) {
        this.creature = creature;
        this.limit = limit;
    }

    @Override
    public void tick() {
        if (!creature.isPersistent()) {
            lived++;
        }
        if (lived >= limit) {
            creature.remove();
        }
    }
}

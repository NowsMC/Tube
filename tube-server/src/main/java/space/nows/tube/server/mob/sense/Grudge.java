package space.nows.tube.server.mob.sense;

import net.minestom.server.entity.LivingEntity;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.trait.Trait;

public final class Grudge implements Trait {

    private final Creature creature;
    private final int minTicks;
    private final int extraTicks;
    private LivingEntity against;
    private int left;

    public Grudge(Creature creature, int minTicks, int maxTicks) {
        this.creature = creature;
        this.minTicks = minTicks;
        this.extraTicks = maxTicks - minTicks + 1;
    }

    public void provoke(LivingEntity by) {
        against = by;
        left = minTicks + creature.random().nextInt(extraTicks);
    }

    public boolean holds(LivingEntity entity) {
        return entity != null && entity == against;
    }

    public boolean angry() {
        return against != null;
    }

    public void forgive() {
        against = null;
        left = 0;
    }

    @Override
    public void tick() {
        if (against == null) {
            return;
        }
        if (against.isDead() || against.isRemoved() || --left <= 0 && creature.target() != against) {
            forgive();
        }
    }
}

package space.nows.tube.server.mob.sense;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;

import java.util.HashSet;
import java.util.Set;

public final class Sight {

    private final EntityCreature creature;
    private final Set<Integer> seen = new HashSet<>();
    private final Set<Integer> unseen = new HashSet<>();

    public Sight(EntityCreature creature) {
        this.creature = creature;
    }

    public void forget() {
        seen.clear();
        unseen.clear();
    }

    public boolean canSee(Entity target) {
        int id = target.getEntityId();
        if (seen.contains(id)) {
            return true;
        }
        if (unseen.contains(id)) {
            return false;
        }
        boolean visible = target.getInstance() == creature.getInstance() && creature.hasLineOfSight(target, false);
        (visible ? seen : unseen).add(id);
        return visible;
    }
}

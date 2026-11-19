package space.nows.tube.server.mob.kind.golem;

import net.minestom.server.entity.Entity;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureKind;

abstract class Golem implements CreatureKind {

    static boolean enemy(Entity entity) {
        return entity instanceof Creature creature && creature.kind().disposition().hostile();
    }

    @Override
    public boolean leavesWhenFar(Creature creature) {
        return false;
    }
}

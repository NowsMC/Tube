package space.nows.tube.server.mob.kind.monster;

import net.minestom.server.coordinate.Point;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureKind;
import space.nows.tube.server.mob.Disposition;
import space.nows.tube.server.mob.sense.Daylight;

public abstract class Monster implements CreatureKind {

    @Override
    public Disposition disposition() {
        return Disposition.HOSTILE;
    }

    @Override
    public float walkPreference(Creature creature, Point at) {
        return -Daylight.pathCost(creature.getInstance(), at);
    }
}

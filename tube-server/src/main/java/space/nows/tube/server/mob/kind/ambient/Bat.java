package space.nows.tube.server.mob.kind.ambient;

import net.minestom.server.entity.EntityType;
import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureKind;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.Disposition;

public final class Bat implements CreatureKind {

    private static final CreatureStats STATS = CreatureStats.mob(6, 0.7);

    @Override
    public EntityType type() {
        return EntityType.BAT;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public Disposition disposition() {
        return Disposition.PASSIVE;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        creature.addTrait(new Roost(creature));
    }
}

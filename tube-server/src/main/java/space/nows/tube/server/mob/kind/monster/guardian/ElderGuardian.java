package space.nows.tube.server.mob.kind.monster.guardian;

import net.minestom.server.entity.EntityType;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;

public final class ElderGuardian extends Guardian {

    private static final CreatureStats STATS = CreatureStats.monster(80, 0.3).withDamage(8);
    private static final int BEAM_TICKS = 60;

    @Override
    public EntityType type() {
        return EntityType.ELDER_GUARDIAN;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    protected boolean elder() {
        return true;
    }

    @Override
    protected int beamTicks() {
        return BEAM_TICKS;
    }

    @Override
    public boolean leavesWhenFar(Creature creature) {
        return false;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        super.configure(creature, brain);
        creature.addTrait(new Curse(creature));
    }
}

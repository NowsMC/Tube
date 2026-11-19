package space.nows.tube.server.mob.kind.water;

import net.minestom.server.entity.EntityType;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureKind;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.Disposition;
import space.nows.tube.server.mob.brain.combat.SwimStrike;
import space.nows.tube.server.mob.brain.idle.SwimAbout;
import space.nows.tube.server.mob.brain.target.Retaliate;

public final class Nautilus implements CreatureKind {

    private static final CreatureStats STATS = CreatureStats.mob(15, 1.0).withDamage(3).withKnockbackResistance(0.3);
    private static final double CRUISE = 0.06;
    private static final double CHASE = 0.12;

    @Override
    public EntityType type() {
        return EntityType.NAUTILUS;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public Disposition disposition() {
        return Disposition.NEUTRAL;
    }

    @Override
    public boolean aquatic() {
        return true;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        brain.add(2, new SwimStrike(creature, CHASE));
        brain.add(4, new SwimAbout(creature, CRUISE));
        brain.add(1, new Retaliate(creature));
    }
}

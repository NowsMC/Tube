package space.nows.tube.server.mob.kind.water;

import net.minestom.server.entity.EntityType;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureKind;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.Disposition;
import space.nows.tube.server.mob.brain.combat.SwimStrike;
import space.nows.tube.server.mob.brain.flee.SwimAway;
import space.nows.tube.server.mob.brain.idle.LookAround;
import space.nows.tube.server.mob.brain.idle.SwimAbout;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.target.Retaliate;

public final class Dolphin implements CreatureKind {

    private static final CreatureStats STATS = CreatureStats.mob(10, 1.2).withDamage(3);
    private static final double CRUISE = 0.12;
    private static final double CHASE = 0.18;

    @Override
    public EntityType type() {
        return EntityType.DOLPHIN;
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
        brain.add(4, new SwimAbout(creature, CRUISE));
        brain.add(4, new LookAround(creature));
        brain.add(5, WatchNearby.players(creature, 6f));
        brain.add(6, new SwimStrike(creature, CHASE));
        brain.add(9, new SwimAway(creature, entity -> entity.getEntityType() == EntityType.GUARDIAN, 8f, CRUISE, CRUISE));
        brain.add(1, new Retaliate(creature, EntityType.GUARDIAN).alertingOthers());
    }
}

package space.nows.tube.server.mob.kind.water;

import net.minestom.server.entity.EntityType;
import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.combat.SwimStrike;
import space.nows.tube.server.mob.brain.idle.SwimAbout;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.brain.target.Retaliate;
import space.nows.tube.server.mob.kind.monster.Monster;

public final class ZombieNautilus extends Monster {

    private static final CreatureStats STATS = CreatureStats.monster(15, 1.1).withDamage(3);
    private static final double CRUISE = 0.06;
    private static final double CHASE = 0.13;

    @Override
    public EntityType type() {
        return EntityType.ZOMBIE_NAUTILUS;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public boolean aquatic() {
        return true;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        brain.add(2, new SwimStrike(creature, CHASE));
        brain.add(4, new SwimAbout(creature, CRUISE));
        brain.add(1, new Retaliate(creature).alertingOthers());
        brain.add(2, HuntNearest.players(creature, true));
    }
}

package space.nows.tube.server.mob.kind.monster.breeze;

import net.minestom.server.entity.EntityType;
import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.brain.target.Retaliate;
import space.nows.tube.server.mob.kind.monster.Monster;

public final class Breeze extends Monster {

    private static final CreatureStats STATS = CreatureStats.monster(30, 0.63).withDamage(3).withFollowRange(24);

    @Override
    public EntityType type() {
        return EntityType.BREEZE;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        brain.add(1, new Gale(creature));
        brain.add(5, Wander.onLand(creature, 0.4));
        brain.add(6, WatchNearby.players(creature, 8f));
        brain.add(1, new Retaliate(creature));
        brain.add(2, HuntNearest.players(creature, true));
    }
}

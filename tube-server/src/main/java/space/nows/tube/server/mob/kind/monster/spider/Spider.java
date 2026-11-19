package space.nows.tube.server.mob.kind.monster.spider;

import net.minestom.server.entity.EntityType;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.combat.Pounce;
import space.nows.tube.server.mob.brain.flee.AvoidNearby;
import space.nows.tube.server.mob.brain.idle.LookAround;
import space.nows.tube.server.mob.brain.idle.StayAfloat;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.brain.target.Retaliate;
import space.nows.tube.server.mob.kind.monster.Monster;

public class Spider extends Monster {

    private static final CreatureStats STATS = CreatureStats.monster(16, 0.3);

    @Override
    public EntityType type() {
        return EntityType.SPIDER;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        brain.add(1, new StayAfloat(creature));
        brain.add(2, AvoidNearby.type(creature, EntityType.ARMADILLO, 6f, 1.0, 1.2));
        brain.add(3, new Pounce(creature, 0.4f));
        brain.add(4, new SpiderBite(creature));
        brain.add(5, Wander.onLand(creature, 0.8));
        brain.add(6, WatchNearby.players(creature, 8f));
        brain.add(6, new LookAround(creature));
        brain.add(1, new Retaliate(creature));
        brain.add(2, HuntNearest.players(creature, true).when(Spiders::dark));
        brain.add(3, HuntNearest.of(creature, EntityType.IRON_GOLEM, true).when(Spiders::dark));
    }
}

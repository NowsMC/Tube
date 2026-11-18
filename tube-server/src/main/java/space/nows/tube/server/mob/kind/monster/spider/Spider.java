package space.nows.tube.server.mob.kind.monster.spider;

import net.minestom.server.entity.EntityType;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.ai.GoalSelector;
import space.nows.tube.server.mob.ai.combat.Pounce;
import space.nows.tube.server.mob.ai.flee.AvoidNearby;
import space.nows.tube.server.mob.ai.idle.LookAround;
import space.nows.tube.server.mob.ai.idle.StayAfloat;
import space.nows.tube.server.mob.ai.idle.Wander;
import space.nows.tube.server.mob.ai.idle.WatchNearby;
import space.nows.tube.server.mob.ai.target.HuntNearest;
import space.nows.tube.server.mob.ai.target.Retaliate;
import space.nows.tube.server.mob.kind.monster.Monster;

public final class Spider extends Monster {

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
    public void configure(Creature creature, GoalSelector goals, GoalSelector targets) {
        goals.add(1, new StayAfloat(creature));
        goals.add(2, AvoidNearby.type(creature, EntityType.ARMADILLO, 6f, 1.0, 1.2));
        goals.add(3, new Pounce(creature, 0.4f));
        goals.add(4, new SpiderBite(creature));
        goals.add(5, Wander.onLand(creature, 0.8));
        goals.add(6, WatchNearby.players(creature, 8f));
        goals.add(6, new LookAround(creature));
        targets.add(1, new Retaliate(creature));
        targets.add(2, HuntNearest.players(creature, true).when(Spiders::dark));
        targets.add(3, HuntNearest.of(creature, EntityType.IRON_GOLEM, true).when(Spiders::dark));
    }
}

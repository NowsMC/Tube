package space.nows.tube.server.mob.kind.monster;

import net.minestom.server.entity.EntityType;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.ai.GoalSelector;
import space.nows.tube.server.mob.ai.combat.ArmsRaisedAttack;
import space.nows.tube.server.mob.ai.idle.LookAround;
import space.nows.tube.server.mob.ai.idle.Wander;
import space.nows.tube.server.mob.ai.idle.WatchNearby;
import space.nows.tube.server.mob.ai.target.HuntNearest;
import space.nows.tube.server.mob.ai.target.Retaliate;
import space.nows.tube.server.mob.trait.SunBurn;

public final class Zombie extends Monster {

    private static final CreatureStats STATS = CreatureStats.monster(20, 0.23).withFollowRange(35).withDamage(3).withArmor(2);

    @Override
    public EntityType type() {
        return EntityType.ZOMBIE;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, GoalSelector goals, GoalSelector targets) {
        goals.add(3, new ArmsRaisedAttack(creature, 1.0, false));
        goals.add(7, Wander.onLand(creature, 1.0));
        goals.add(8, WatchNearby.players(creature, 8f));
        goals.add(8, new LookAround(creature));
        targets.add(1, new Retaliate(creature).alertingOthers(EntityType.ZOMBIFIED_PIGLIN));
        targets.add(2, HuntNearest.players(creature, true));
        targets.add(3, HuntNearest.of(creature, entity -> entity.getEntityType() == EntityType.VILLAGER
                || entity.getEntityType() == EntityType.WANDERING_TRADER, 10, false, entity -> true));
        targets.add(3, HuntNearest.of(creature, EntityType.IRON_GOLEM, true));
        targets.add(5, Turtles.hatchlingHunt(creature));
        creature.addTrait(new SunBurn(creature));
    }
}

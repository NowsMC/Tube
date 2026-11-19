package space.nows.tube.server.mob.kind.monster.illager;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.brain.flee.AvoidNearby;
import space.nows.tube.server.mob.brain.idle.StayAfloat;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.brain.target.Retaliate;
import space.nows.tube.server.mob.kind.monster.Monster;
import space.nows.tube.server.mob.kind.monster.Raiders;

import java.util.Set;

abstract class Illager extends Monster {

    private static final Set<EntityType> VILLAGERS = Set.of(EntityType.VILLAGER, EntityType.WANDERING_TRADER);

    static boolean villager(Entity entity) {
        return VILLAGERS.contains(entity.getEntityType());
    }

    protected abstract boolean seesVillagers();

    protected int caution() {
        return 1;
    }

    protected abstract void behaviors(Creature creature, Brain brain);

    @Override
    public void configure(Creature creature, Brain brain) {
        brain.add(0, new StayAfloat(creature));
        brain.add(caution(), AvoidNearby.type(creature, EntityType.CREAKING, 8f, 1.0, 1.2));
        behaviors(creature, brain);
        brain.add(8, Wander.anywhere(creature, 0.6));
        brain.add(10, WatchNearby.of(creature, entity -> entity instanceof Creature, 8f));
        brain.add(1, new Retaliate(creature, Raiders.types()).alertingOthers());
        brain.add(2, HuntNearest.players(creature, true));
        brain.add(3, HuntNearest.of(creature, Illager::villager, 10, seesVillagers(), target -> true));
        brain.add(3, HuntNearest.of(creature, EntityType.IRON_GOLEM, true));
    }
}

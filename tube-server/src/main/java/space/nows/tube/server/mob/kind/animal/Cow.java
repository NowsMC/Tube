package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.ai.GoalSelector;
import space.nows.tube.server.mob.ai.idle.LookAround;
import space.nows.tube.server.mob.ai.idle.Panic;
import space.nows.tube.server.mob.ai.idle.StayAfloat;
import space.nows.tube.server.mob.ai.idle.Tempted;
import space.nows.tube.server.mob.ai.idle.Wander;
import space.nows.tube.server.mob.ai.idle.WatchNearby;

import java.util.Set;
import java.util.random.RandomGenerator;

public final class Cow extends Animal {

    private static final CreatureStats STATS = CreatureStats.animal(10, 0.2);
    private static final Set<Material> FOOD = Set.of(Material.WHEAT);

    @Override
    public EntityType type() {
        return EntityType.COW;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, GoalSelector goals, GoalSelector targets) {
        goals.add(0, new StayAfloat(creature));
        goals.add(1, new Panic(creature, 2.0));
        goals.add(3, new Tempted(creature, 1.25, FOOD));
        goals.add(5, Wander.onLand(creature, 1.0));
        goals.add(6, WatchNearby.players(creature, 6f));
        goals.add(7, new LookAround(creature));
    }

    @Override
    public void prepare(Creature creature, RandomGenerator random) {
        creature.set(DataComponents.COW_VARIANT, FarmClimate.at(creature.getInstance(), creature.getPosition()).variant());
    }
}
